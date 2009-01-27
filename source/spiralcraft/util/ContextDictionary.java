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

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
import spiralcraft.text.markup.MarkupHandler;
import spiralcraft.text.markup.MarkupParser;
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
    protected synchronized String get(String name)
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

  private static final MarkupParser substitutionParser
    =new MarkupParser("${","}",'\\');
  
  public static String substitute(String raw)
    throws ParseException
  {
    final StringBuffer ret=new StringBuffer();

      substitutionParser.parse
        (raw
        ,new MarkupHandler()
        {

          @Override
          public void handleContent(
            CharSequence text)
            throws ParseException
          { ret.append(text);
          }

          @Override
          public void handleMarkup(
            CharSequence code)
            throws ParseException
          { 
            String substitution
              =ContextDictionary.getInstance().find
                (code.toString(),"${"+code.toString()+"}");
            if (substitution!=null)
            { ret.append(substitution);
            }
          }

          @Override
          public void setPosition(
            ParsePosition position)
          { } 
        }
        ,null
        );

    return ret.toString();
  }
  
  
  
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
  
  
  private final ContextDictionary parent;
  private final HashMap<String,String> map=new HashMap<String,String>();
  
  public ContextDictionary(ContextDictionary parent)
  { this.parent=parent;
  }
  
  public String find(String name,String defaultVal)
  { 
    String val=find(name);
    return val!=null?val:defaultVal;
  }
  
  public String find(String name)
  {
    String val=null;
    if (parent!=null)
    { val=parent.find(name);
    }
    if (val==null)
    { val=get(name);
    }    
    return val;
    
  }
  
  protected synchronized String get(String name)
  { return map.get(name);
  }
  
  public synchronized void set(String name,String value)
  { map.put(name, value);
  }
  
}
