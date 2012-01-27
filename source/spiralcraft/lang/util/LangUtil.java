//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.lang.util;

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Functor;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.functions.FromString;
import spiralcraft.lang.reflect.BeanReflector;

public class LangUtil
{

  /**
   * Find the nearest instance of the specified class in the context or null,
   *   if it is not present.
   * 
   * @param <T>
   * @param clazz
   * @param context
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T findInstance(Class<T> clazz,Focus<?> context)
  {
    if (context==null)
    { return null;
    }
    
    Focus<?> focus
      =context.findFocus(BeanReflector.getInstance(clazz).getTypeURI());
    if (focus!=null && focus.getSubject()!=null)
    { return (T) focus.getSubject().get();
    }
    
    return null;
  }
  
  /**
   * Find the nearest instance of the specified class in the context or 
   *   throw a BindException.
   *   
   * @param <T>
   * @param clazz
   * @param context
   * @return
   */
  public static <T> T assertInstance(Class<T> clazz,Focus<?> context)
    throws BindException
  {
    T ret=findInstance(clazz,context);
    if (ret==null)
    { 
      throw new BindException
        ("Could not find an instance of class "+clazz+" in context.");
    }
    return ret;
  }
  
  
  @SuppressWarnings("unchecked")
  public static <T> Channel<T> findChannel(Class<T> clazz,Focus<?> context)
  {
    if (context==null)
    { return null;
    }
    
    Focus<?> focus
      =context.findFocus(BeanReflector.getInstance(clazz).getTypeURI());
    if (focus!=null)
    { return (Channel<T>) focus.getSubject();
    }
    
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Channel<T> findChannel(URI typeURI,Focus<?> context)
  {
    if (context==null)
    { return null;
    }
    
    Focus<?> focus
      =context.findFocus(typeURI);
    if (focus!=null)
    { return (Channel<T>) focus.getSubject();
    }
    
    return null;
  }

  public static <T> Channel<T> assertChannel
    (Class<T> clazz, Focus<?> focusChain)
    throws BindException
  {
    Channel<T> ret=findChannel(clazz,focusChain);
    if (ret==null)
    {
      throw new BindException
        ("Could not find a provider of class "+clazz+" in context.");
    }
    return ret;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Focus<T> findFocus(Class<T> clazz,Focus<?> context)
  {
    if (context==null)
    { return null;
    }
    
    return (Focus<T>) 
      context.findFocus(BeanReflector.getInstance(clazz).getTypeURI());

  }
  
 
  /**
   * Return the result of a single evaluation of a self-contained Expression 
   * 
   * @param <T>
   * @param expression
   * @return
   * @throws BindException
   */
  public static <T> T eval(String expression)
    throws BindException
  { return new SimpleFocus<T>().bind(Expression.<T>create(expression)).get();
  }
  
  /**
   * Return the constructor channel for the type referenced by the Reflector
   * 
   * @param <T>
   * @param reflector
   * @param context
   * @return
   * @throws BindException
   */
  @SuppressWarnings("unchecked")
  public static <T> Channel<T> constructorFor
    (Reflector<T> reflector,Focus<?> context)
    throws BindException
  { 
    if (reflector instanceof Functor)
    { return ((Functor<T>) reflector).bindChannel(context,new Channel[0]);
    }
    else
    { return null;
    }
  }

  /**
   * <p>Return a Channel that provides data of the specified type, performing
   *   a type conversion if required, and throwing an exception if a 
   *   conversion cannot be made.
   * </p>
   * 
   * @param <T>
   * @param in
   * @param type
   * @return
   * @throws BindException 
   */
  @SuppressWarnings("unchecked")
  public static <T> Channel<T> ensureType
    (Channel<?> in,Class<T> type,Focus<?> context)
    throws BindException
  {
    if (type.isAssignableFrom(in.getContentType()))
    { return (Channel<T>) in;
    }
    else if (in.getContentType()==String.class)
    { 
      return new FromString<T>(type)
        .bindChannel((Channel<String>) in,context,null);
    }
    else
    { 
      throw new BindException
        ("Cannot convert from "+in.getReflector().getTypeURI()
          +" ("+in.getContentType().getName()+")  to "
          +" "+type.getName()
          );
    }
  }
  

}
