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


import spiralcraft.common.LifecycleException;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.session.DataSessionFocus;
import spiralcraft.data.session.DataSession;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Provides a stateful typed Context that can be shared between scenarios
 * </p>
 *
 * @author mike
 *
 * @param <Tresult>
 */
public class Session<Tresult>
  extends Scenario<Task,Tresult>
{

  protected Scenario<Task,Tresult> scenario;
  
  protected ThreadLocalChannel<DataSession> sessionChannel
    =new ThreadLocalChannel<DataSession>
      (BeanReflector.<DataSession>getInstance(DataSession.class));
  
  protected DataSessionFocus dataSessionFocus;
  protected Type<? extends DataComposite> type;
  
  public void setScenario(Scenario<Task,Tresult> scenario)
  { this.scenario=scenario;
  }
  
  public void setType(Type<? extends DataComposite> type)
  { this.type=type;
  }
  
  @Override
  protected Task task()
  {
    return new AbstractTask<Tresult>()
    {
        
      @Override
      public void work()
      {
        sessionChannel.push(null);
        dataSessionFocus.reset();
        
        TaskCommand<Task,Tresult> command
          =scenario.command();
        if (debug)
        { log.fine("Executing "+command);
        }
        command.execute();
        if (command.getResult()!=null)
        {
          for (Tresult result : command.getResult())
          { addResult(result);
          }
        }
        if (command.getException()!=null)
        { addException(command.getException());
        }
        sessionChannel.pop();
      }
    };
  }

  @Override
  public void start()
    throws LifecycleException
  {
    super.start();
    scenario.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    scenario.stop();
    super.stop();
  }
  
  
  @Override
  protected Focus<?> bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    focusChain=super.bindChildren(focusChain);
    dataSessionFocus
      =new DataSessionFocus(focusChain,sessionChannel,type);
    scenario.bind(dataSessionFocus);
    return scenario.bind(dataSessionFocus);
  }

}
