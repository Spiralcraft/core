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

import java.net.URI;

import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;

/**
 * Runs another scenario in the context of an arbitrary component.
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class Model<R>
  extends Scenario<Task,R>
{

  private Scenario<?,R> scenario;

  private URI targetTypeURI;
  private URI targetURI;
  
  private AbstractXmlObject<?,?> target;
  
  public void setScenario(Scenario<?,R> scenario)
  { this.scenario=scenario;
  }
  
  @Override
  protected Task task()
  {
    
    
    return new AbstractTask<R>()
    {

      @Override
      protected void work()
        throws InterruptedException
      {            
        try
        {
          target.runInContext
            (new Delegate<TaskCommand<?,R>>()
              {
                @Override
                public TaskCommand<?, R> run()
                  throws DelegateException
                {
                  TaskCommand<?,R> command=
                    scenario.command();
                  command.execute();
                  return command;
                }
              }
            );
        }
        catch (DelegateException x)
        { addException(x);
        }
        
        
      }      
    };
    
  }

  @Override
  public Focus<?> bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    target=AbstractXmlObject.create
      (targetTypeURI,targetURI);
    focusChain=target.bind(focusChain);
    scenario.bind(focusChain);
    return focusChain;
  }

}
