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
 * Integrates an arbitrary component into the Scenario chain
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class Reference
  extends Scenario
{

  private URI targetTypeURI;
  private URI targetURI;
  
  private AbstractXmlObject<?,?> target;
  

  public void setTargetTypeURI(URI targetTypeURI)
  { this.targetTypeURI=targetTypeURI;
  }
  
  public void setTargetURI(URI targetURI)
  { this.targetURI=targetURI;
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
        try
        {
          TaskCommand command
            =target.runInContext
            (new Delegate<TaskCommand>()
              {
                @Override
                public TaskCommand run()
                  throws DelegateException
                { 
                  if (chain!=null)
                  { 
                    TaskCommand command=chain.command();
                    command.execute();
                    return command;
                  }
                  else
                  { return null;
                  }
                }
              }
            );
          addResult(command);
          if (command.getException()!=null)
          { 
            addException(command.getException());
            return;
          }
        }
        catch (DelegateException x)
        {
          addException(x);
          return;
        }
        
      }      
    };
    
  }

  @Override
  public void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    target=AbstractXmlObject.create
      (targetTypeURI,targetURI);
    focusChain=target.bind(focusChain);
    super.bindChildren(focusChain);
  }

}
