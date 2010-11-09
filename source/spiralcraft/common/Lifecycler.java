//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.common;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

public final class Lifecycler
{
  
  private static final ClassLog log
    =ClassLog.getInstance(Lifecycler.class);

  public static void start(Lifecycle[] la)
    throws LifecycleException
  {
    if (la==null)
    { return;
    }
    
    int i=0;
    try
    {
      for (Lifecycle l:la)
      {
        l.start();
        i++;
      }
    }
    catch (LifecycleException x)
    {
      log.log(Level.WARNING,"Failed to start "+la[i],x);
      for (;--i>0;)
      { 
        try
        { la[i].stop();
        }
        catch (LifecycleException y)
        { log.log(Level.DEBUG,"Error stopping after failed start "+la[i],y);
        }
      }
      throw x;
    }
  }
  
  public static void stop(Lifecycle[] la)
    throws LifecycleException
  {
    if (la==null)
    { return;
    }
    
    int i=la.length;

    for (;--i>0;)
    {
      try
      { la[i].stop();
      }
      catch (LifecycleException y)
      { log.log(Level.DEBUG,"Error stopping "+la[i],y);
      }
    }
    
  }
}