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



import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import spiralcraft.log.Level;

import spiralcraft.cli.BeanArguments;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;
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

  private boolean consoleControl;
  private ConsoleController consoleController;
  
  public void setConsoleControl(boolean consoleControl)
  { this.consoleControl=consoleControl;
  }
  
  @Override
  public void start()
    throws LifecycleException
  {
    super.start();
    if (consoleControl)
    { 
      consoleController=new ConsoleController();
      consoleController.start();
    }
  }
    
  @Override
  public void stop()
    throws LifecycleException
  {
    if (consoleController!=null)
    { consoleController.stop();
    }
    super.stop();
  }

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

  class ConsoleController
    implements Runnable
  {
    private Thread consoleThread;
    private boolean done;
    
    public void start()
    { 
      consoleThread=new Thread(this);
      consoleThread.start();
    }
    
    public void run()
    {
      LineNumberReader consoleLines
        =new LineNumberReader
          (new InputStreamReader
            (ExecutionContext.getInstance().in())
          );
      while (!done)
      {
        String line=null;
        try
        { 
          line=consoleLines.readLine();
          if (line==null)
          {
            done=true;
            log.fine("End of console input reached");
            break;
          }
          if (logLevel.isFine())
          { log.fine("Received console command: "+line);
          }
          if (line.equals("quit"))
          {
            log.info("Quitting on console command");
            done=true;
            Daemon.this.terminate();
            break;
          }
        }
        catch (IOException x)
        { 
          log.log(Level.WARNING,"IOException reading console input",x);
          break;
        }
      }
    }
    
    public synchronized void stop()
    { 
      this.done=true;
      try
      { ExecutionContext.getInstance().in().close();
      }
      catch (IOException x)
      { log.log(Level.INFO,"Caught exception closing console input",x);
      }
      consoleThread.interrupt();
    }
  }
}
