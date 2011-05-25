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

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.namespace.PrefixedName;
import spiralcraft.common.namespace.UnresolvedPrefixException;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

/**
 * Integrates an arbitrary component into the Scenario chain
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class Reference<Tresult>
  extends Chain<Void,Tresult>
{

  private URI targetTypeURI;
  private URI targetURI;
  private URI referenceURI;
  
  private AbstractXmlObject<?,?> target;
  
  { addChainResult=true;
  }
  
  

  public void setReference(PrefixedName reference) 
    throws UnresolvedPrefixException
  { this.referenceURI=reference.resolve().toURIPath();
  }
  
  public void setTargetTypeURI(URI targetTypeURI)
  { this.targetTypeURI=targetTypeURI;
  }
  
  public void setTargetURI(URI targetURI)
  { this.targetURI=targetURI;
  }
  
  @Override
  protected Task task()
  {
    
    
    return new ChainTask()
    {
      @Override
      protected void work()
        throws InterruptedException
      {         
        target.push();
        try
        { super.work();
        }
        finally
        { target.pop();
        }
        
      }      
    };
    
  }

  @Override
  public void start()
    throws LifecycleException
  {
    target.start();
    super.start();
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    target.stop();
    super.stop();
  }

  
  @Override
  public void bindChildren(
    Focus<?> focusChain)
    throws ContextualException
  {
    target
      =referenceURI==null
      ?AbstractXmlObject.create(targetTypeURI,targetURI)
      :AbstractXmlObject.instantiate(referenceURI);
    try
    { focusChain=target.bind(focusChain);
    }
    catch (ContextualException x)
    { throw new BindException
        ("Error resolving contextual "+targetTypeURI+" : "+targetURI,x);
    }
    super.bindChildren(focusChain);
  }

}
