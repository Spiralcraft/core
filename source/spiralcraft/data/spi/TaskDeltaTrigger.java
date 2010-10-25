//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.data.spi;


import spiralcraft.data.DeltaTuple;
import spiralcraft.data.access.DeltaTrigger;
import spiralcraft.data.transaction.TransactionException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.task.Scenario;
import spiralcraft.task.TaskCommand;


/**
 * <p>Specifies a Task to be performed when Entity data is modified.
 * </p>
 * 
 * @author mike
 *
 */
public class TaskDeltaTrigger
  extends DeltaTrigger
{
 
  private Scenario<DeltaTuple,DeltaTuple> task;

  
  public void setTask(Scenario<DeltaTuple,DeltaTuple> task)
  { this.task=task;
  }
  
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    if (task!=null)
    { task.bind(focusChain);
    }
    return focusChain;
  }
  
  @Override
  public DeltaTuple trigger()
    throws TransactionException
  { 
    DeltaTuple tuple=null;
    if (task!=null)
    {
      TaskCommand<DeltaTuple,DeltaTuple> command
        =task.command();
      command.execute();
      if (command.getException()!=null)
      { 
        throw new TransactionException
          ("Error running trigger",command.getException());
      }
      tuple=command.getResult();
    }
    
    
    return tuple;
    
  }

}
