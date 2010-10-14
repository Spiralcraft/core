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

  protected TaskCommand<?,?> command;
  protected boolean addResult;
  
  @Override
  protected void work()
    throws InterruptedException
  {
    command.execute();
    if (command.getException()!=null)
    { addException(command.getException());
    }
    if (addResult)
    { addResult(command);
    }
  }

}
