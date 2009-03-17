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
//
package spiralcraft.task;

import spiralcraft.command.CommandAdapter;


/**
 * <p>A Command that encapsulates the execution of a specific type of Task.
 * </p>
 * 
 * @author mike
 *
 */
public class TaskCommand<Ttask extends Task,Tresult>
  extends CommandAdapter<Scenario<Ttask,Tresult>,Tresult>
{
  
  protected Ttask task;

  public TaskCommand()
  { }
  
  public TaskCommand(Ttask task)
  { this.task=task;
  }
  
  public Ttask getTask()
  { return task;
  }
  
  @Override
  public void run()
  { 
    
    try
    { 
      if (task==null)
      { throw new IllegalStateException("No task specified");
      }
      else
      { 
        task.run();
        setException(task.getException());
      }
    }
    catch (RuntimeException x)
    { 
      setException(x);
      throw x;
    }
  }
  
}
