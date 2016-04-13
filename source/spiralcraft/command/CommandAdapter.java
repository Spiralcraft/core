//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.command;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
/**
 * <p>Provides a simple mechanism for implementing the Command interface by
 *   implementing an abstract run() method.
 * </p>
 * 
 * <p>Deals with synchronization, setting and storing of flags, catching 
 *   unhandled exceptions, and cloning.
 * </p>
 * 
 * <p>The implementor may override the execute() method to perform
 *   pre or post execution housekeeping.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class CommandAdapter<Ttarget,Tcontext,Tresult>
  implements Command<Ttarget,Tcontext,Tresult>
{
 
  private static final ClassLog log=ClassLog.getInstance(CommandAdapter.class);
  
  private boolean started;
  private boolean completed;
  
  private Exception exception;
  private Tresult result;
  private Ttarget target;
  private Tcontext context;
  protected String name;
  
  @Override
  public boolean isStarted()
  { return started;
  }
  
  @Override
  public boolean isCompleted()
  { return completed;
  }
  
  @Override
  public Exception getException()
  { return exception;
  }
  
  @Override
  public void setContext(Tcontext context)
  { this.context=context;
  }

  @Override
  public void setTarget(Ttarget target)
  { this.target=target; 
  }
  
  @Override
  public Ttarget getTarget()
  { return target;
  }
  
  @Override
  public Tcontext getContext()
  { return context;
  }
  
  
  @Override
  public Tresult getResult()
  { return result;
  }
  
  protected synchronized void notifyStarted()
  { started=true;
  }
  
  protected synchronized void notifyCompleted()
  { completed=true;
  }
  
  protected void setException(Exception exception)
  { this.exception=exception;
  }
  
  protected void setResult(Tresult result)
  { this.result=result;
  }
  
  /**
   * <p>Run the function associated with the command.
   * </p>
   */
  protected abstract void run();

  /**
   * <p>Execute the command
   * </p>
   * 
   * <p>Note: to implement functionality, override the run()
   *   method instead.
   * </p>
   */
  @Override
  public final void execute()
  {
    if (started)
    { 
      log.log(Level.WARNING,"Ignoring redundant command execution "+getClass().getName());
      return;
    }
    
    notifyStarted();
    try
    { run();
    }
    catch (Exception x)
    { setException(x);
    }
    finally
    { notifyCompleted();
    }
  }
  
  @Override
  @SuppressWarnings("unchecked") // Cast Object.clone() result
  public synchronized Command<Ttarget,Tcontext,Tresult> clone()
  {
    if (started)
    {
      throw new IllegalStateException
        ("Cannot clone a Command that has been executed already");
    }
    try
    { return (Command<Ttarget,Tcontext,Tresult>) super.clone();
    }
    catch (CloneNotSupportedException x)
    { throw new RuntimeException("Unexpected exception during clone",x);
    }
  }
  
  @Override
  public boolean isUndoable()
  { return false;
  }
  
  @Override
  public void undo()
  { throw new IllegalStateException("Command cannot be undone");
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+(name!=null?name:"(anon)");
  }
}
