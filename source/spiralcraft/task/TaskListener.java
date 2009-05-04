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
package spiralcraft.task;


public interface TaskListener
{
 
  /**
   * Called when a task is started
   * 
   * @param event
   */
  public void taskStarted(TaskEvent event);
  
  /**
   * Called when a task is completed
   * 
   * @param event
   */
  public void taskCompleted(TaskEvent event);

  /**
   * Called when a task generated a result
   * 
   * @param event
   * @param result
   */
  public void taskAddedResult(TaskEvent event,Object result);
  
  /**
   * Called when a task threw an Exception 
   * 
   * @param event
   * @param result
   */
  public void taskThrewException(TaskEvent event,Exception exception);
}
