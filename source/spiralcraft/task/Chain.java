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

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;



/**
 * <p>A Scenario which provides a chaining mechanism to incorporate
 *   child scenarios which may bind to this Scenario
 * </p>
 * 
 * <p>The chaining mechanism allows a set of Scenarios to be specified
 *   as a simple array via the setChain() method to simplify the construction
 *   of complex nested Scenarios.
 * </p>
 * 
 * <p>If a non-Chain Scenario is included in the chain, it will be run in a 
 *   Sequence prior to the next Scenario encountered. When a Chain scenario is
 *   encountered after a non-Chain scenario, it will be the last item
 *   in the Sequence, and further Scenarios will be chained within its
 *   context.
 * </p>
 * 
 * @author mike
 */
public class Chain<Tcontext,Tresult>
  extends Scenario<Tcontext,Tresult>
{

  
  protected Scenario<?,?> chain;
  protected boolean addChainCommandAsResult=false;
  protected boolean addChainResult;
  
  public void setAddChainResult(boolean addChainResult)
  { this.addChainResult=addChainResult;
  }
  
    
  protected class ChainTask
    extends CommandTask
  {
    
    { 
      addCommandAsResult=addChainCommandAsResult;
      addCommandResult=addChainResult;
    }
    
    @Override
    protected void work()
      throws InterruptedException
    { 
      if (chain!=null && exception==null)
      {
        command=chain.command();
        super.work();
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
    if (chain!=null)
    { chain.bind(focusChain);
    }
    if (addChainResult)
    { storeResults=true;
    }
  }
  
  
  @Override
  protected Task task()
  { return new ChainTask();
  }
  
  
  public void chain(Scenario<?,?> chain)
  { this.chain=chain;
  }
  
  /**
   * Wrap a single task. Allows for the result to be returned if 
   *   addChainResult=true
   * 
   * @param chain
   */
  public void setChain(Scenario<?,?> chain)
  { this.chain=chain;
  }
  
  public void setSequence(Scenario<?,?>[] scenarios)
  {
    if (scenarios.length==1)
    { chain(scenarios[0]);
    }
    else
    {
      Sequence<?> sequence=new Sequence<Object>();
      chain(sequence);
      for (Scenario<?,?> scenario: scenarios)
      { sequence.addScenario(scenario);
      }
    }
  }
  
 
  
  @Override
  public void start()
    throws LifecycleException
  { 
    super.start();
    if (chain!=null)
    { chain.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (chain!=null)
    { chain.stop();
    }
    super.stop();
  }  
  
}
