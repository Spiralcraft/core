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
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;


/**
 * <p>A abstract scenario that has children meant to run in a sequence
 * </p>
 * 
 * 
 * @author mike
 */
public abstract class Chain<Tcontext,Tresult>
  extends Scenario<Tcontext,Tresult>
{

  
  protected Scenario<?,?> chain;
  protected boolean addChainCommandAsResult=false;
  protected boolean addChainResult;
  protected Binding<Tresult> resultX;
  
  public void setAddChainResult(boolean addChainResult)
  { this.addChainResult=addChainResult;
  }
  
    
  public void setResultX(Binding<Tresult> resultX)
  { 
    this.resultX=resultX;
    this.storeResults=true;
    this.addChainResult=false;
    this.addChainCommandAsResult=false;
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
        if (resultX!=null)
        { 
          addResult(resultX.get());
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
  @SuppressWarnings({ "unchecked" })
  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  { 
    if (chain!=null)
    { chain.bind(focusChain);
    }
    if (addChainResult)
    {
      storeResults=true;
      if (chain.getResultReflector()!=null && resultReflector==null)
      {
        if (debug)
        { log.fine("Inheriting chain result- result is "+chain.getResultReflector()+" ("+getDeclarationInfo()+")");
        }
        this.resultReflector=(Reflector<Tresult>) chain.getResultReflector();
      }
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
