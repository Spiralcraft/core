//
// Copyright (c) 2009,2010 Michael Toth
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

import spiralcraft.command.CommandFactory;
import spiralcraft.command.CommandScheduler;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.vfs.context.ContextResourceMap;

/**
 * <p>Runs a Scenario periodically. The Scenario is added to the
 *   focus chain.
 * </p>
 * 
 * @author mike
 *
 */

@SuppressWarnings({"unchecked","rawtypes"})
public class TaskScheduler
  extends CommandScheduler
  implements Contextual
{

  protected Scenario scenario;
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    // Make sure that we transfer the resourceMap from the binding thread
    resourceMap=ContextResourceMap.get();
    
    return scenario.bind(focusChain);
  }
  
  /**
   * Specify the Scenario to schedule
   * 
   * @param scenario
   */
  public void setScenario(Scenario scenario)
  { 
    this.scenario=scenario;
    super.setCommandFactory(scenario);
  }
  
  @Override
  public void setCommandFactory(CommandFactory factory)
  { 
    throw new UnsupportedOperationException
      ("Use setScenario(Scenario) to supply unbound task context");
  }
  
  
}
