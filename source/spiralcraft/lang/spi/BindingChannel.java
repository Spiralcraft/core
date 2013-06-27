//
// Copyright (c) 2011,2012 Michael Toth
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

import spiralcraft.common.Coercion;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.functions.FromString;
import spiralcraft.lang.kit.CoercionChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.NumericCoercion;
import spiralcraft.util.string.StringConverter;

/**
 * <p>Assigns the value of an expression bound to a "source" context
 *   to a location in a "target" context. 
 * </p>
 * 
 * <p>This is usually expressed in the form "target := source", where the
 *   ":=" operator signifies "binding" 
 * </p>
 *
 * @param <T>
 */
public class BindingChannel<T>
  extends SourcedChannel<T,T>
{
  private static final ClassLog log
    =ClassLog.getInstance(BindingChannel.class);
  

  public static final BindingChannel<?>[] bind
    (Expression<?>[] expressions,Focus<?> focus)
    throws BindException
  {
    if (expressions==null)
    { return null;
    }
    
    BindingChannel<?>[] ret=new BindingChannel<?>[expressions.length];
    int i=0;
    for (Expression<?> x:expressions)
    {
      Channel<?> channel=focus.bind(x);
      if (channel instanceof BindingChannel)
      { ret[i++]=(BindingChannel<?>) channel;
      }
      else
      {
        throw new BindException
          ("Expected expression in the form:  target:=sourceExpr");
      }
    }
    return ret;
  }
  
  public static final void bindTarget
    (BindingChannel<?>[] channels,Focus<?> focus)
    throws BindException
  {
    if (channels==null)
    { return;
    } 
    
    for (BindingChannel<?> channel:channels)
    { channel.bindTarget(focus);
    }
  }
  
  public static final void apply(BindingChannel<?>[] channels)
  {
    if (channels==null)
    { return;
    } 

    for (BindingChannel<?> channel:channels)
    { channel.get();
    }
  }
  
  public static final void applyReverse(BindingChannel<?>[] channels)
  {
    if (channels==null)
    { return;
    } 

    for (BindingChannel<?> channel:channels)
    { channel.applyReverse();
    }
  }
  
  public static final Channel<?>[] sources(BindingChannel<?>[] channels)
  { 
    Channel<?>[] sources=new Channel<?>[channels.length];
    int i=0;
    for (BindingChannel<?> channel:channels)
    { sources[i++]=channel.getSource();
    }
    return sources;
  }
  
  private final Expression<T> sourceX;
  private final Expression<T> targetX;
  private Channel<T> targetChannel;
  private Binding<T> sourceBinding;
  private StringConverter<T> converter;
  private Coercion<?,T> coercion;
  private Channel<T> filteredSource;

  public BindingChannel
    (Focus<?> focus,Expression<T> sourceX,Expression<T> targetX)
    throws BindException
  {
    super(focus.bind(sourceX));
    this.sourceX=sourceX;
    this.targetX=targetX;
  }


  @Override
  public <X> Channel<X> 
    resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { 
    if (targetChannel==null)
    { throw new BindException("Target '"+targetX+"' not bound");
    }
    return super.resolve(focus,name,params);
  }

  @Override
  public Reflector<T> getReflector()
  { return super.getReflector();
  }

  @SuppressWarnings({ "rawtypes", "unchecked"
    })
  public void bindTarget(Focus<?> targetFocus)
    throws BindException
  { 
    targetChannel=targetFocus.bind(targetX);

// 
//  writable is not a bind-time property
//     
//    if (!targetChannel.isWritable())
//    { 
//      throw new BindException
//        ("Target '"+targetX+"' is not writable: "
//        +targetChannel.toString()
//        );
//    }
    
    if (!targetChannel.getReflector()
          .isAssignableFrom(source.getReflector())
       )
    { 

      if (targetChannel.getContentType().isAssignableFrom(Binding.class))
      { 
        // Provide a dynamic reference to the specified source data.
        sourceBinding=new Binding<T>(source);
      }
      else
      {
        if (source.getContentType()==String.class)
        { 
          converter=StringConverter.getInstance(targetChannel.getContentType());
          if (converter==null)
          {
            throw new BindException
              ("Argument type mismatch: "
                +targetChannel.getReflector().getTypeURI()
                +" ("+targetChannel.getContentType().getName()+")"
                +" cannot be automatically converted from a string"
              );
          }
          filteredSource
            =new FromString<T>(targetChannel.getReflector(),converter)
              .bindChannel((Channel<String>) source,targetFocus,null);
        }
        else if (Number.class.isAssignableFrom(source.getContentType()))
        {
          coercion
            =(Coercion<?,T>) NumericCoercion.instance(targetChannel.getContentType());
    
          if (coercion==null)
          {
            throw new BindException
              ("Cannot assign a "+source.getReflector().getTypeURI()
              +" to a location of type "+targetChannel.getReflector().getTypeURI()
              );
          }
          
          filteredSource
            =new CoercionChannel(targetChannel.getReflector(),source,coercion);

        }
        else
        {
          // XXX: Consider automatic type conversion here e.g. for unboxing 
          //  primitive arrays
          throw new BindException
            ("Argument type mismatch: "
              +targetChannel.getReflector().getTypeURI()
              +" ("+targetChannel.getContentType().getName()+")"
              +" is not assignable from `"+sourceX+"` "+source.getReflector().getTypeURI()
              +" ("+source.getContentType().getName()+")"
            );
        }
      }
    }
    else
    { filteredSource=source;
    }
  }

  public Channel<T> getTarget()
  { 
    assertTarget();
    return targetChannel;
  }
  
  public boolean applyReverse()
  { 
    assertTarget();
    return filteredSource.set(targetChannel.get());
  }
  

  
  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  {
    assertTarget();
    T val;
    val=sourceBinding!=null?(T) sourceBinding:filteredSource.get();
    if (!targetChannel.set(val))
    { log.warning("Bound assignment failed");
    }
    return val;
  }

  @Override
  protected boolean store(
    T val)
  throws AccessException
  { 
    assertTarget();
    boolean set=targetChannel.set(val);
    filteredSource.set(val);
    return set;
  }

  private void assertTarget()
  {
    if (targetChannel==null)
    { 
      throw new IllegalStateException
        ("Target '"+targetX+"' not bound. The ':=' operator may not be used"
        +" in this scenario"
        );
    }
  }
  @Override
  public boolean isWritable()
  { return targetChannel.isWritable();
  }
}
