//
// Copyright (c) 2011 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.lang.kit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.parser.StructField;
import spiralcraft.lang.parser.StructNode;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.ViewCache;
import spiralcraft.lang.spi.ViewState;
import spiralcraft.lang.spi.VoidChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;

public class Computation<I,R,P>
{

  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(Computation.class);

  protected final ThreadLocalChannel<R> result;
  protected final ThreadLocalChannel<I> input;
  protected final Channel<I> source;
  
  protected Transform transform;
      
  
  protected final Channel<P> functionChannel;  
  protected final ViewCache viewCache;
  @SuppressWarnings("unused")
  private boolean debug;

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Computation
    (Channel<I> source
    ,Focus<?> focus
    ,Expression<P> function
    )
    throws BindException
  {
    viewCache=new ViewCache(focus);
    this.source=source;
    
    input
      =new ThreadLocalChannel<I>(source,true);
        
    focus=focus.telescope(input);
    
    focus.addFacet(viewCache.bind(focus));
    

    if (function.getRootNode() instanceof StructNode)
    {
      ArrayList<Channel<?>> keys=new ArrayList<Channel<?>>();
      
      StructNode structNode=(StructNode) function.getRootNode();
      for (StructField field : structNode.getFields())
      {
        int lastSize=viewCache.getSize();
        if (field.getSource()==null)
        { 
          throw new BindException
            ("All fields in reduction struct must be bound");
        }
        
        // Provide same relative position to bind keys,
        //   while disallowing having a key depend on the result struct
        Focus<?> keyFocus=focus.telescope(new VoidChannel());
        
        Channel<?> fieldChan
          =keyFocus.bind(Expression.create(field.getSource()));
        
        if (viewCache.getSize()==lastSize)
        { 
//          log.fine("Key: "+new Expression(field.getSource()));
          keys.add(fieldChan);
        }
        else
        { 
//          log.fine("Function: "+new Expression(field.getSource()));
          viewCache.setSize(lastSize);
        }
      }
      
      
      functionChannel=focus.bind(function);
      
      if (keys.size()==0)
      { 
        transform
          =new ComputeScalar
            ((Reflector<R>) functionChannel.getReflector());
      }
      else
      { 
        transform
          =new ComputeGroup
            (aggregateReflector(functionChannel)
            ,new KeyChannel(keys)
            );
      }
    }
    else
    {

      functionChannel=focus.bind(function);
      if (viewCache.getSize()>0)
      { 
        transform
          =new ComputeScalar
            ((Reflector<R>) functionChannel.getReflector());
      }
      else
      { 
        transform
          =new ComputeDistinct
            (aggregateReflector(functionChannel));
      }
    }
    
//    log.fine("Transform="+transform);
    
    result=new ThreadLocalChannel(transform.getReflector());
    
  }
  
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void push()
  { transform.push();
  }

  public void pop()
  { transform.pop();
  }
  
  public void checkpoint()
  { transform.checkpoint();
  }
  
  public void update()
  { transform.update();
  }
  
  public Channel<R> getResultChannel()
  { return result;
  }
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected  R createArray(ArrayList<P> output)
  {
    Class pclass=functionChannel.getContentType();
    if (pclass.isPrimitive())
    { pclass=ClassUtil.boxedEquivalent(pclass);
    }
    return (R) 
      output.toArray((Object[]) Array.newInstance(pclass,output.size()));
  }
  
  @SuppressWarnings("unchecked")
  protected Reflector<R> aggregateReflector(Channel<P> projection)
    throws BindException
  { 
    return (Reflector<R>) 
      ArrayReflector.getInstance(projection.getReflector());
  }
  
  class KeyChannel
    extends AbstractChannel<Object>
  {
    private final ArrayList<Channel<?>> sources;
    
    public KeyChannel(ArrayList<Channel<?>> sources)
    { 
      super(BeanReflector.getInstance(Object.class));
      this.sources=sources;
    }

    @Override
    protected Object retrieve()
    {
      Object[] key=new Object[sources.size()];
      int i=0;
      for (Channel<?> source:sources)
      { key[i++]=source.get();
      }
      return ArrayUtil.asKey(key);
    }

    @Override
    protected boolean store(
      Object val)
      throws AccessException
    { throw new UnsupportedOperationException("KeyChannel is read only");
    }
    
    
  }
    
  abstract class Transform
  {
    private Reflector<R> reflector;
    
    public Transform(Reflector<R> resultReflector)
    { reflector=resultReflector;
    }
    
    public Reflector<R> getReflector()
    { return reflector;
    }
    
    public void push()
    { 
      input.push(null);
      viewCache.push();
      result.push();
      
    }
    
    public void pop()
    { 
      result.pop();
      viewCache.pop();
      input.pop();
    }
    
    public abstract void checkpoint();
    
    public abstract void update();
  }
  
  class ComputeDistinct
    extends Transform
  {
    private final ThreadLocal<LinkedHashSet<P>> intermediate
      =new InheritableThreadLocal<LinkedHashSet<P>>();
    
    public ComputeDistinct(Reflector<R> resultReflector)
    { super(resultReflector);
    }

    @Override
    public void push()
    { 
      intermediate.set(new LinkedHashSet<P>());
      super.push();
    }
    
    @Override
    public void pop()
    { 
      super.pop();
      intermediate.remove();
    }
    
    
    @Override
    public void update()
    {
      input.set(source.get());
      viewCache.touch();
      intermediate.get().add(functionChannel.get());
      
    }
    
    @Override
    public void checkpoint()
    { 
      viewCache.checkpoint();
      result.set(createArray(new ArrayList<P>(intermediate.get())));
    }
  }   
  
  class ComputeGroup
    extends Transform
  {
    
    private ThreadLocal<LinkedHashMap<Object,ViewStateRef<P,I>>> groups
      =new InheritableThreadLocal<LinkedHashMap<Object,ViewStateRef<P,I>>>();
    
    private final KeyChannel keyChannel;
    
    public ComputeGroup
      (Reflector<R> resultReflector
      ,KeyChannel keyChannel
      )
    { 
      
      super(resultReflector);
      
      this.keyChannel=keyChannel;
    }

    @Override
    public void push()
    { 
      groups.set(new LinkedHashMap<Object,ViewStateRef<P,I>>());
      super.push();
    }
    
    @Override
    public void pop()
    { 
      super.pop();
      groups.remove();
    }
    
    
    @Override
    public void update()
    {
      I item=source.get();
      input.set(item);
      Object key=keyChannel.get();
      LinkedHashMap<Object,ViewStateRef<P,I>> groups
        =this.groups.get();
      ViewStateRef<P,I> stateRef=groups.get(key);
      if (stateRef!=null)
      { viewCache.set(stateRef.states);
      }
      else
      { 
//        log.fine("No find key "+key);
        viewCache.init();
        stateRef=new ViewStateRef<P,I>();
        stateRef.states=viewCache.get();
        groups.put(key,stateRef);
      }
      stateRef.last=item;
      
      viewCache.touch();
      
      stateRef.data=functionChannel.get();
      
    }
    
    
    @Override
    public void checkpoint()
    {
//      log.fine("Checkpoint");
      ArrayList<P> output=new ArrayList<P>();
      for (ViewStateRef<P,I> stateRef : groups.get().values())
      {
        input.set(stateRef.last);
        viewCache.set(stateRef.states);
        viewCache.checkpoint();
        output.add(functionChannel.get());
//        log.fine("Added "+functionChannel.get());
      }
      result.set(createArray(output)); 
    }
  }  
  
  class ComputeScalar
    extends Transform
  {

    public ComputeScalar
      (Reflector<R> resultReflector
      )
    { super(resultReflector);
    }

    @Override
    public void push()
    {
      super.push();
      viewCache.init();
    }

    
    
    @SuppressWarnings("unchecked")
    @Override
    public void update()
    {
      input.set(source.get());
      viewCache.touch();
      result.set((R) functionChannel.get());
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public void checkpoint()
    {
      viewCache.checkpoint();
      result.set((R) functionChannel.get());
    }

  }  
  

}

class ViewStateRef<Tstate,TinputItem>
{
  ViewState<?>[] states;
  Tstate data;
  TinputItem last;
}
