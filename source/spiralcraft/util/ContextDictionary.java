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

//import spiralcraft.log.ClassLog;
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
//  private static final ClassLog log
//    =ClassLog.getInstance(ContextDictionary.class);
  
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
          private ParsePosition position;
          
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
                (code.toString(),null);
            if (substitution!=null)
            { ret.append(substitution);
            }
            else
            { 
              
              throw new ParseException
                ("Context property '"+code.toString()+"' not found: ",position);
            }
            
//            if (substitution!=code.toString())
//            { log.fine(code.toString()+" >>> "+substitution);
//            }
            
          }

          @Override
          public void setPosition(
            ParsePosition position)
          { this.position=position;
            
          } 
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
  private final HashMap<String,String> map;
  private final boolean local;
  
  
  public ContextDictionary(ContextDictionary parent)
  { 
    this.parent=parent;
    map=new HashMap<String,String>();
    local=false;
  }
  
  public ContextDictionary
    (ContextDictionary parent,HashMap<String,String> map,boolean local)
  { 
    this.parent=parent;
    this.map=map;
    this.local=local;
  }

  public String find(String name,String defaultVal)
  { 
    String val=find(name);
    return val!=null?val:defaultVal;
  }
  
  public String find(String name)
  {
    String val=null;
    if (local)
    { val=get(name);
    }
    if (val==null && parent!=null)
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
