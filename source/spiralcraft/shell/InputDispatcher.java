//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.shell;

import java.io.IOException;
import java.io.InputStream;

import spiralcraft.log.Level;
import spiralcraft.util.thread.AsyncRunner;

/**
 * <p>Blocks on an InputStream and forwards events to a handler
 * </p>
 * 
 * @author mike
 *
 */
public class InputDispatcher
  extends AsyncRunner
{
  
  protected InputStream in;
  protected int pollMs;
  
  public void setInputStream(InputStream in)
  { this.in=in;
  }
  
  /**
   * <p>Indicate how often the source InputStream.available() method will be
   *  polled. A non-zero value avoids blocking reads, which may have
   *  side effects. 
   * </p>
   * 
   * <p>This option is not compatible with InputStreams that do not provide
   *   a functional available() method (ie. the method always returns 0).
   * </p>
   * 
   * @param The period in milliseconds between checks to 
   *   InputStream.available()
   */
  public void setPollPeriodMs(int pollMs)
  { this.pollMs=pollMs;
  }
  
  /**
   * <p>Whether the Thread should allow the VM to terminate while it is
   *   still running.
   * </p>
   * @param daemon Whether a daemon thread should be created
   */
  public void setDaemon(boolean daemon)
  { this.daemon=daemon;
  }
  
  @Override
  public void run()
  {
    if (debug)
    { log.fine("Running");
    }
    
    while (true)
    { 
      int i=-1;
      try
      {
        while (pollMs>0 && in.available()<1)
        { 
          try
          { Thread.sleep(pollMs);
          }
          catch (InterruptedException x)
          { }
          if (this.shouldStop())
          { return;
          }
          
        }
        
        i=in.read();
        if (i==-1)
        {   
          if (debug)
          { log.fine("End of input");
          }
          break;
        }
        if (debug)
        { log.fine("["+(char) i+"]="+i);
        }
      }
      catch (IOException x)
      { 
        if (debug)
        { log.log(Level.DEBUG,"IOException reading input",x);
        }
        break;
      }      
    }
    
    
  }
  
}
