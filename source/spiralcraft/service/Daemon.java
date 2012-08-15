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
package spiralcraft.service;



import spiralcraft.cli.BeanArguments;
import spiralcraft.common.ContextualException;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionException;

import spiralcraft.lang.SimpleFocus;


/**
 * <p>An executable that starts, responds to events, and terminates
 *   upon receipt of an applicable signal.
 * </p>
 *
 * <p>A daemon is the outermost layer of the services framework.
 * </p>
 */
public class Daemon
  extends Application
  implements Executable
{

  
  @Override
  public final void execute(String ... args)
    throws ExecutionException
  {
    try
    { 
      new BeanArguments<Daemon>(this).process(args);
      
      try
      { bind(new SimpleFocus<Void>());
      }
      catch (ContextualException x)
      { throw new ExecutionException("Error binding",x);
      }
      
      push();
      try
      { run();
      }
      finally
      { pop();
      }

    }
    catch (RuntimeException x)
    { throw new ExecutionException("Exception executing app",x);
    }
    
  }

}
