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
package spiralcraft.log;

import java.util.WeakHashMap;

import spiralcraft.util.ContextDictionary;
import spiralcraft.util.Path;




public class ClassLog
  extends ContextLog
{
  
  
  private static final WeakHashMap<Class<?>,ClassLog> map
    =new WeakHashMap<Class<?>,ClassLog>();

  private static final ClassLog log=new ClassLog(ClassLog.class.getName());
  
  /**
   * Return the initial level at which the given class should output
   *   debug messages. Defaults to OFF, but may be set using a 
   *   system property [classname].debugLevel
   *   
   * @param subject
   * @return
   */
  public static final Level 
    getInitialDebugLevel(Class<?> subject,Level defaultLevel)
  { 
    String level=ContextDictionary.getInstance()
      .find(subject.getName()+".debugLevel"
            ,defaultLevel!=null?defaultLevel.getName():"OFF"
            );
    
    Level lvl=Level.valueOf(level);
    if (lvl.isDebug())
    { log.debug("Log Level = "+lvl.getName()+" for "+subject.getName());
    }
    return Level.valueOf(level);
  }
  
  
  public static synchronized final ClassLog getInstance(Class<?> subject)
  {
    ClassLog ret=map.get(subject);
    if (ret!=null)
    { return ret;
    }
    
    ret=new ClassLog(subject.getName());
//    try
//    { ret.start();
//    }
//    catch (LifecycleException x)
//    { 
//      throw new IllegalArgumentException
//        ("Error starting log for "+subject.getName(),x);
//    }
    map.put(subject,ret);
    return ret;
  }

//  static
//  {
//    Runtime.getRuntime().addShutdownHook
//      (new Thread()
//      {
//        @Override
//        public void run()
//        { 
//          for (ClassLog log:map.values())
//          { 
//            try
//            { log.stop();
//            }
//            catch (LifecycleException x)
//            { x.printStackTrace();
//            }
//          }
//        }
//      }
//      
//      );
//  }


  ClassLog(String className)
  { this.context=new Path(className,'/');
  }
  
  public void fine(String message)
  { log(FINE,message,null,1);
  }

  public void fine(String message,Exception exception)
  { log(Level.FINE,message,exception,1);
  }
    
  public void fine(String message,int stackOffset)
  { log(FINE,message,null,1+stackOffset);
  }
  
  public void trace(String message)
  { log(TRACE,message,null,1);
  }
  

  public void debug(String message)
  { log(DEBUG,message,null,1);
  }
  
  public void config(String message)
  { log(CONFIG,message,null,1);
  }

  public void info(String message)
  { log(INFO,message,null,1);
  }

  public void warning(String message)
  { log(WARNING,message,null,1);
  }

  public void severe(String message)
  { log(SEVERE,message,null,1);
  }

   

}
