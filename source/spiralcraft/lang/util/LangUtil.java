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

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;

public class LangUtil
{

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
  public static <T> Focus<T> findFocus(Class<T> clazz,Focus<?> context)
  {
    if (context==null)
    { return null;
    }
    
    return (Focus<T>) 
      context.findFocus(BeanReflector.getInstance(clazz).getTypeURI());

  }
}
