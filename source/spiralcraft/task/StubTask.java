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

/**
 * An stub implementation of a task for development/example purposes
 */
public class StubTask
  extends AbstractTask
{
  
  protected void execute()
  {
    setUnitsInTask(1);
    setOpsInUnit(100);
    setUnitsCompletedInTask(0);
    setOpsCompletedInUnit(0);
    setCurrentUnitTitle("Counting");
    
    try
    {
      for (int i=0;i<100;i++)
      { 
        Thread.currentThread().sleep(250);
        setOpsCompletedInUnit(i);
        setCurrentOpTitle(Integer.toString(i));
      }
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
  }
  
}
