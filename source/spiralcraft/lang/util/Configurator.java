//
// Copyright (c) 2009 Michael Toth
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

import java.util.HashMap;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * Provides a mechanism to dynamically translate textual expression/value pairs
 *   to assignments on properties of a target object 
 * 
 * @author mike
 *
 */
public class Configurator<T>
{
  public static <X> Configurator<X> forBean(X target)
  {
    Configurator<X> ci
      =new Configurator<X>(BeanReflector.<X>getInstance(target.getClass()));
    ci.set(target);
    return ci;
    
  }
  
  private final SimpleChannel<T> channel;
  private final Focus<T> focus;
  
  private HashMap<String,DictionaryBinding<?>> bindingMap
    =new HashMap<String,DictionaryBinding<?>>();
  
  public Configurator(Reflector<T> type)
  { 
    this.channel=new SimpleChannel<T>(type);
    this.focus=new SimpleFocus<T>(channel);
  }
 
  public void set(T target)
  { channel.set(target);
  }

  @SuppressWarnings("unchecked")
  private DictionaryBinding getBinding(String targetExpression)
  {
    DictionaryBinding binding=bindingMap.get(targetExpression);
    if (binding==null)
    { 
      Expression expression=Expression.create(targetExpression);
      try
      {
        binding=new DictionaryBinding();
        binding.setTarget(expression);
        binding.bind(focus);
        bindingMap.put(targetExpression, binding);
      }
      catch (BindException x)
      { throw new IllegalArgumentException("Error binding "+targetExpression,x);
      }
    }
    return binding;
    
  }
  
  public void set(String targetExpression,String value)
  { getBinding(targetExpression).set(value);
  }
  
  public String get(String targetExpression)
  { return getBinding(targetExpression).get();
  }
  
  public Class<?> getType(String targetExpression)
  { return getBinding(targetExpression).getTargetChannel().getContentType();
  }
  
 
}
