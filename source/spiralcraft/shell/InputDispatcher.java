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
  
  public void setInputStream(InputStream in)
  { this.in=in;
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
