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
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
//import spiralcraft.lang.reflect.BeanReflector;
//import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.log.Log;

/**
 * <p>A Scenario is a Task factory. It Implements a runnable operation in the 
 *   form of a Task that is bound into a Context via the FocusChain. 
 * </p>
 *
 * <p>Scenarios may reference and contain other Scenarios to compose a program
 *   of execution from components.
 * </p>
 * 
 * @author mike
 */
public abstract class Scenario<Ttask extends Task,Tresult>
  implements Lifecycle,FocusChainObject
{

  protected Log log=ClassLog.getInstance(getClass());
//  protected ThreadLocalChannel<TaskCommand<Ttask,Tresult>> commandChannel;
  protected boolean debug;
  
  
  /**
   * 
   * @return A command which runs this Scenario when invoked. Used to wire 
   *   together behavioral elements in a set of Scenarios. The Command
   *   target is the scenario, and the result is implementation specific.
   */
  public Command<Scenario<Ttask,Tresult>,Tresult> 
    command()
  { 
    Ttask task=task();
    if (debug)
    { task.setDebug(debug);
    }
  	return new TaskCommand<Ttask,Tresult>(task);
  }

  protected abstract Ttask task();
  
//  protected Focus<TaskCommand<Ttask,Tresult>> 
//    bindCommand(Focus<?> focusChain,Class<TaskCommand<Ttask,Tresult>> clazz)
//  {
//    commandChannel
//      =new ThreadLocalChannel<TaskCommand<Ttask,Tresult>>
//        (BeanReflector.<TaskCommand<Ttask,Tresult>>getInstance(clazz));
//    return focusChain.chain(commandChannel);
//  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;

  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }

  @Override
  public void start()
    throws LifecycleException
  { log.log(Level.FINE,this+": starting");
  }

  @Override
  public void stop()
    throws LifecycleException
  { log.log(Level.FINE,this+": stopping");
  }
}
