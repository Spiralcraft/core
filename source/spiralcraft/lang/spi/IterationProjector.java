//
// Copyright (c) 1998,2010 Michael Toth
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
package spiralcraft.lang.spi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.parser.StructField;
import spiralcraft.lang.parser.StructNode;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;

/**
 * Utility to build a projection Channel on an Iterable
 * 
 * @author mike
 *
 * @param <I>
 * @param <P>
 */
public class IterationProjector<I,P,R>
{
  private static final ClassLog log
    =ClassLog.getInstance(IterationProjector.class);

  public final Channel<R> result;
  
  protected final IterationDecorator<I[],I> iterable;
    
  protected final ThreadLocalChannel<I> channel;
  
  @SuppressWarnings("unchecked")
  protected final ThreadLocalChannel<IterationCursor> cursorChannel;
        
  protected final Focus<I> telefocus;
    
  protected final Channel<P> functionChannel;  
  protected final ViewCache viewCache;
  protected final boolean reduce;
  
  @SuppressWarnings("unchecked")
  public IterationProjector
    (Channel<I[]> source
    ,Focus<?> focus
    ,Expression<P> function
    ,boolean reduce
    )
    throws BindException
  {
    viewCache=new ViewCache(focus);
    this.reduce=reduce;
    
    iterable
      =source.getReflector()
        .<IterationDecorator>decorate(source,IterationDecorator.class);
    if (iterable==null)
    { throw new BindException("Source is not iterable: "+source.getReflector());
    }
    
    
    
    channel
      =new ThreadLocalChannel<I>(iterable.getComponentReflector());
        
    telefocus=focus.telescope(channel);
    
    cursorChannel
      =new ThreadLocalChannel<IterationCursor>
        (BeanReflector.<IterationCursor>getInstance(IterationCursor.class));
    
    telefocus.addFacet(new SimpleFocus(cursorChannel));
    telefocus.addFacet(viewCache.bind(telefocus));
    
    if (!reduce)
    {
    
      functionChannel=telefocus.bind(function);
    
      result=new MapChannel
        (aggregateReflector(functionChannel));
    }
    else
    { 
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
          Focus<?> keyFocus=telefocus.telescope(new VoidChannel());
          
          Channel<?> fieldChan
            =keyFocus.bind(new Expression(field.getSource()));
          
          if (viewCache.getSize()==lastSize)
          { keys.add(fieldChan);
          }
          else
          { viewCache.setSize(lastSize);
          }
        }
        
        
        functionChannel=telefocus.bind(function);
        
        if (keys.size()==0)
        { 
          result
            =new ReduceScalarChannel
              ((Reflector<R>) functionChannel.getReflector());
        }
        else
        { 
          result
            =new ReduceGroupChannel
              (aggregateReflector(functionChannel)
              ,new KeyChannel(keys)
              );
        }
      }
      else
      {

        functionChannel=telefocus.bind(function);
        if (viewCache.getSize()>0)
        { 
          result
            =new ReduceScalarChannel
              ((Reflector<R>) functionChannel.getReflector());
        }
        else
        { 
          result
            =new ReduceDistinctChannel
              (aggregateReflector(functionChannel));
        }
      }
    }
  }
  

  
  
  @SuppressWarnings("unchecked")
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
    
  class MapChannel
    extends AbstractChannel<R>
  {
    public MapChannel(Reflector<R> resultReflector)
    { super(resultReflector);
    }

    @Override
    protected R retrieve()
    {
      ArrayList<P> output=new ArrayList<P>();

      IterationCursor<I> it=iterable.iterator();
      cursorChannel.push(it);
      channel.push(null);
      viewCache.push();
      viewCache.init();
      try
      {
        while (it.hasNext())
        {
          I item=it.next();
          channel.set(item);
          viewCache.touch();
          output.add(functionChannel.get());
        }
        viewCache.checkpoint();
        return createArray(output); 
      }
      finally
      { 
        viewCache.pop();
        channel.pop();
        cursorChannel.pop();
      }

    }

    @Override
    protected boolean store(
      R val)
    throws AccessException
    { return false;
    }

  }  

  class ReduceDistinctChannel
    extends AbstractChannel<R>
  {
    public ReduceDistinctChannel(Reflector<R> resultReflector)
    { super(resultReflector);
    }

    @Override
    protected R retrieve()
    {
      LinkedHashSet<P> output=new LinkedHashSet<P>();

      IterationCursor<I> it=iterable.iterator();
      cursorChannel.push(it);
      channel.push(null);
      viewCache.push();
      viewCache.init();
      try
      {
        while (it.hasNext())
        {
          I item=it.next();
          channel.set(item);
          viewCache.touch();
          output.add(functionChannel.get());
        }
        viewCache.checkpoint();
        return createArray(new ArrayList<P>(output)); 
      }
      finally
      { 
        viewCache.pop();
        channel.pop();
        cursorChannel.pop();
      }

    }

    @Override
    protected boolean store(
      R val)
    throws AccessException
    { return false;
    }

  }   
  
  class ReduceGroupChannel
    extends AbstractChannel<R>
  {
    
    private final KeyChannel keyChannel;
    
    public ReduceGroupChannel
      (Reflector<R> resultReflector
      ,KeyChannel keyChannel
      )
    { 
      
      super(resultReflector);
      
      this.keyChannel=keyChannel;
    }

    @Override
    protected R retrieve()
    {
      LinkedHashMap<Object,ViewStateRef<P,I>> groups
        =new LinkedHashMap<Object,ViewStateRef<P,I>>();

      IterationCursor<I> it=iterable.iterator();
      cursorChannel.push(it);
      channel.push(null);
      viewCache.push();
      try
      {
        while (it.hasNext())
        {
          I item=it.next();
          channel.set(item);
          
          Object key=keyChannel.get();
          if (debug)
          { log.fine("Adding key "+key);
          }
          
          ViewStateRef<P,I> stateRef=groups.get(key);
          
          if (stateRef!=null)
          { viewCache.set(stateRef.states);
          }
          else
          { 
            viewCache.init();
            stateRef=new ViewStateRef<P,I>();
            stateRef.states=viewCache.get();
            groups.put(key,stateRef);
          }
          stateRef.last=item;
          
          viewCache.touch();
          
          stateRef.data=functionChannel.get();
          if (debug)
          { log.fine("Group: "+stateRef.data);
          }          
        }
        
        ArrayList<P> output=new ArrayList<P>();
        for (ViewStateRef<P,I> stateRef : groups.values())
        {
          channel.set(stateRef.last);
          viewCache.set(stateRef.states);
          viewCache.checkpoint();
          output.add(functionChannel.get());
        }
        return createArray(output); 
      }
      finally
      { 
        viewCache.pop();
        channel.pop();
        cursorChannel.pop();
      }

    }

    @Override
    protected boolean store(
      R val)
    throws AccessException
    { return false;
    }

  }  
  
  class ReduceScalarChannel
    extends AbstractChannel<R>
  {

    public ReduceScalarChannel
      (Reflector<R> resultReflector
      )
    { 

      super(resultReflector);

    }

    @SuppressWarnings("unchecked")
    @Override
    protected R retrieve()
    {
      P output=null;

      IterationCursor<I> it=iterable.iterator();
      cursorChannel.push(it);
      channel.push(null);
      viewCache.push();
      viewCache.init();
      try
      {
        while (it.hasNext())
        {
          I item=it.next();
          channel.set(item);
          viewCache.touch();
          output=functionChannel.get();
        }
        viewCache.checkpoint();
        return (R) output; 
      }
      finally
      { 
        viewCache.pop();
        channel.pop();
        cursorChannel.pop();
      }

    }

    @Override
    protected boolean store(
      R val)
    throws AccessException
    { return false;
    }

  }  
  
}

class ViewStateRef<Tstate,TinputItem>
{
  ViewState<?>[] states;
  Tstate data;
  TinputItem last;
}

