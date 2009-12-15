//
// Copyright (c) 2009 Michael Toth
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

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.log.Level;
import spiralcraft.task.Task;

/**
 * Perform a set of Assignments 
 *  
 * @author mike
 *
 */
public class Assign
  extends Scenario<Void,Void>
{

  protected Assignment<?>[] assignments;
  protected Setter<?>[] setters;
  protected Assignment<?>[] defaultAssignments;
  protected Setter<?>[] defaultSetters;
  

  
  public void setAssignments(Assignment<?>[] assignments)
  { this.assignments=assignments;
  }

  public void setDefaults(Assignment<?>[] defaultAssignments)
  { this.defaultAssignments=defaultAssignments;
  }
  
  @Override
  protected Task task()
  {
    return new AbstractTask()
    {

      @Override
      protected void work()
        throws InterruptedException
      { 
        if (debug)
        { log.log(Level.FINE,this+": applying assignments");
        }
        Setter.applyArray(setters);
        Setter.applyArrayIfNull(defaultSetters);

      }
    };    
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  {  
    setters=Assignment.bindArray(assignments,focusChain);
    defaultSetters=Assignment.bindArray(defaultAssignments,focusChain);
    
    super.bindChildren(focusChain);
  }
}
