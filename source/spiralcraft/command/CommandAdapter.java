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

/**
 * <p>Provides a simple mechanism for implementing the Command interface by
 *   implementing the run() method of the Runnable interface.
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
public abstract class CommandAdapter<Tresult>
  implements Command<Tresult>,Runnable
{
  private boolean started;
  private boolean completed;
  private Exception exception;
  private Tresult result;
  
  public boolean isStarted()
  { return started;
  }
  
  public boolean isCompleted()
  { return completed;
  }
  
  public Exception getException()
  { return exception;
  }
  
  public Tresult getResult()
  { return result;
  }
  
  protected synchronized void notifyStarted()
  { 
    if (started)
    { 
      throw new IllegalStateException
        ("Cannot execute a Command instance more than once");
    }
    started=true;
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
   * <p>Execute the command
   * </p>
   * 
   * <p>Note: to implement functionality, consider overriding the run()
   *   method instead.
   * </p>
   */
  public void execute()
  {
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
  
  @SuppressWarnings("unchecked") // Cast Object.clone() result
  public synchronized Command<Tresult> clone()
  {
    if (started)
    {
      throw new IllegalStateException
        ("Cannot clone a Command that has been executed already");
    }
    try
    { return (Command<Tresult>) super.clone();
    }
    catch (CloneNotSupportedException x)
    { throw new RuntimeException("Unexpected exception during clone",x);
    }
  }
}
