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
package spiralcraft.util.string;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

//import spiralcraft.log.ClassLog;
//import spiralcraft.log.Level;

/**
 * <p>Pools immutable object instances to coalesce references to identical 
 *   objects into a single weak reference to reduce memory and increase 
 *   throughput.
 * </p>
 * 
 * <p>When all references to the object are released, the reference in
 *   this map will be garbage collected
 * </p>
 * 
 * @author mike
 *
 */
public class StringPool
{
  
  public static final StringPool INSTANCE
    =new StringPool();

//  private static final ClassLog log
//    =ClassLog.getInstance(StringPool.class);
//  private static final Level logLevel
//    =ClassLog.getInitialDebugLevel(StringPool.class,Level.INFO);
  
  
  private WeakHashMap<String,WeakReference<String>> map
    =new WeakHashMap<String,WeakReference<String>>();
  
 
  public String get(String value)
  { 
    if (value==null)
    { return null;
    }
    
    if (value.length()==0)
    { return "";
    }
    
    if (value.length()==1)
    { return Character.toString(value.charAt(0)).intern();
    }
    
    synchronized (map)
    {
      WeakReference<String> ref=map.get(value);
      if (ref!=null)
      { 
        String result=ref.get();
        if (result!=null)
        { return result;
        }
      }
      
      value=new String(value);
//      if (logLevel.isFine())
//      { log.fine(value);
//      }
      map.put(value,new WeakReference<String>(value));
    }
    return value;
  }
  
  public String intern(String value)
  { 
    if (value==null)
    { return null;
    }
    
    if (value.length()==0)
    { return "";
    }
    
    if (value.length()==1)
    { return Character.toString(value.charAt(0)).intern();
    }
    
    value=value.intern();
    synchronized (map)
    {
      WeakReference<String> ref=map.get(value);
      if (ref!=null)
      { 
        String result=ref.get();
        if (result!=null)
        { return result;
        }
      }
      
//      if (logLevel.isFine())
//      { log.fine("Intern: "+value);
//      }
      map.put(value,new WeakReference<String>(value));
    }
    return value;
    
  }
  

}
