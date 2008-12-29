//
// Copyright (c) 2008 Michael Toth
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

import spiralcraft.command.Command;
import spiralcraft.common.Lifecycle;
import spiralcraft.lang.FocusChainObject;

/**
 * <p>Implements a runnable operation in the form of a Task that is bound into
 *   a Context via the FocusChain. 
 * </p>
 *
 * <p>Scenarios may reference and contain other Scenarios to compose a program
 *   of execution from components.
 * </p>
 * 
 * @author mike
 */
public interface Scenario
  extends Lifecycle,FocusChainObject
{

  /**
   * <p>Generate a new Task to run this Scenario. The Task may invoke other
   *   Tasks generated from contained and references Scenarios as required.
   * </p>
   * 
   * @return A Task that is ready to be started or run.
   */
  Task task();
  
  Command<? extends Scenario,?> runCommand();
}
