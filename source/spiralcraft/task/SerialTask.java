//
// Copyright (c) 1998,2008 Michael Toth
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

import java.util.List;

//import spiralcraft.log.ClassLog;

/**
 * Executes a number of other Tasks in series
 */
public class SerialTask
  extends MultiTask
{
//  private static final ClassLog log
//    =ClassLog.getInstance(SerialTask.class);
  
  public SerialTask(List<? extends Task> tasks)
  { super(tasks);
  }
 
  
  @Override
  protected void execute()
  {
    setUnitsInTask(tasks.size());
    setOpsInUnit(1);
    setUnitsCompletedInTask(0);
    setOpsCompletedInUnit(0);
    setCurrentUnitTitle("Counting");
    
    for (Task task: tasks)
    { task.run();
    }

  }


  
}
