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

import java.util.ArrayList;

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;



/**
 * <p>A abstract scenario that will run one child
 * </p>
 * 
 * 
 * @author mike
 */
public abstract class Branch<Tcontext,Tresult>
  extends Scenario<Tcontext,Tresult>
{

  protected ArrayList<Scenario<?,?>> children
    =new ArrayList<Scenario<?,?>>(2);
  protected Binding<Tresult> resultX;
  
  public void setResultX(Binding<Tresult> resultX)
  { 
    this.resultX=resultX;
    this.storeResults=true;
  }
  
  protected abstract Scenario<?,?> select();
    
  protected class BranchTask
    extends CommandTask
  {
    @Override
    protected void work()
      throws InterruptedException
    { 
      Scenario<?,?> scenario=select();
      if (scenario!=null && exception==null)
      {
        command=scenario.command();
        super.work();
        if (resultX!=null)
        { addResult(resultX.get());
        }  
        
      }
    }
  }
  
  /**
   * <p>Publish Channels into the Focus chain for use by child Scenarios.
   * </p>
   * 
   * <p>Default implementation binds the rest of the chain. When overriding,
   *   call this method with the Focus that should be published to the rest
   *   of the Chain.
   * </p>
   * 
   * <p>The supplied Focus chain already publishes the Scenario and the
   *   TaskCommand for structural and per-invocation context respectively.
   * </p>
   * 
   * 
   * @param focusChain
   * @throws BindException
   */
  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  { 
    for (Scenario<?,?> scenario : children)
    { scenario.bind(focusChain);
    }

  }
  
  @Override
  protected void bindResult(Focus<?> focusChain)
    throws ContextualException
  {   
    if (resultX!=null)
    { 
      resultX.bind(focusChain);
      resultReflector=resultX.getReflector();
    }
  }
    
  @Override
  protected Task task()
  { return new BranchTask();
  }
    
  @Override
  public void start()
    throws LifecycleException
  { 
    super.start();
    Lifecycler.start(children.toArray(new Scenario<?,?>[children.size()]));
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    Lifecycler.stop(children.toArray(new Scenario<?,?>[children.size()]));
    super.stop();
  }  
  
}
