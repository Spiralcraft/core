//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.util;

import java.util.HashMap;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>A chain of name-value maps, normally associated with the current 
 *   thread context, which allow application components to access locally 
 *   managed properties.
 * </p>
 * 
 * @author mike
 *
 */
public class ContextDictionary
{
  private static final ContextDictionary _SYSTEM_CONTEXT
    =new ContextDictionary(null)
  {
    @Override
    public synchronized String get(String name)
    { return System.getProperty(name);
    }
  
    @Override
    public synchronized void set(String name,String value)
    { System.setProperty(name, value);
    }     
  };
  
  private static ThreadLocalStack<ContextDictionary> _INSTANCE 
    = new ThreadLocalStack<ContextDictionary>(true)
  {
    @Override
    public ContextDictionary defaultValue()
    { return _SYSTEM_CONTEXT;
    }
  };
  
  /**
   * <p>Obtain the thread-local singleton instance of the ContextDictionary
   * </p>
   * 
   * @return
   */
  public static final ContextDictionary getInstance()
  { return _INSTANCE.get();
  }

  /**
   * <p>Set the current thread-local singleton instance of the ContextDictionary
   * </p>
   * 
   * <p>This instance must be reset by calling popInstance() when the operation
   *   is complete
   * </p>
   * 
   * @return
   */
  public static final void pushInstance(ContextDictionary context)
  { _INSTANCE.push(context);
  }
  
  /**
   * <p>Reset the current thread-local singleton instance
   * </p>
   */
  public static final void popInstance()
  { _INSTANCE.pop();
  }
  
  
  private final ContextDictionary defaults;
  private final HashMap<String,String> map=new HashMap<String,String>();
  
  public ContextDictionary(ContextDictionary defaults)
  { this.defaults=defaults;
  }
  
  public String find(String name,String defaultVal)
  { 
    String val=get(name);
    if (val==null && defaults!=null)
    { val=defaults.find(name,defaultVal);
    }
    return val==null?defaultVal:val;
  }
  
  public synchronized String get(String name)
  { return map.get(name);
  }
  
  public synchronized void set(String name,String value)
  { map.put(name, value);
  }
  
}
