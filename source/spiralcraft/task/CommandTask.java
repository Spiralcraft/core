//
// Copyright (c) 2008,2009 Michael Toth
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

/**
 * A Task which runs a command
 * 
 * @author mike
 *
 */
public abstract class CommandTask
  extends AbstractTask
{

  protected volatile TaskCommand<?,?> command;
  protected boolean addCommandAsResult;
  protected boolean addCommandResult;
  
  /**
   * Pass the stop to the command being run, if any
   */
  @Override
  public void stop()
  {
    super.stop();
    TaskCommand<?,?> current=command;
    if (current!=null)
    { current.stop();
    }
  }

  @Override
  protected void work()
    throws InterruptedException
  {
    command.execute();
    if (command.getException()!=null)
    { addException(command.getException());
    }

    if (addCommandResult)
    { addResult(command.getResult());
    }
    else if (addCommandAsResult)
    { addResult(command);
    }
  }

}
