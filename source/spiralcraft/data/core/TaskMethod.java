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
package spiralcraft.data.core;

import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.data.persist.AbstractXmlObject;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

import spiralcraft.task.Scenario;
import spiralcraft.util.refpool.URIPool;

/**
 * Allows Task scenarios to implement data methods
 * 
 * @author mike
 *
 * @param <T>
 * @param <C>
 * @param <R>
 */
public class TaskMethod<T,C,R>
  extends AbstractTaskMethod<T,C,R>
{

  private URI scenarioURI;
  
  public void setScenarioURI(URI scenarioURI)
  { this.scenarioURI=scenarioURI;
  }
  

  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  protected Focus<Scenario<C,R>> bindTask(
    Focus<?> context,
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  {
    
    if (scenarioURI==null)
    { 
      scenarioURI
        =URIPool.create
          (getDataType().getURI().toString()+"_"+getName()+".scenario.xml");
    }
    
    try
    {
    
      AbstractXmlObject<Scenario<C,R>,?> scenarioContainer
        =AbstractXmlObject
          .<Scenario<C,R>>activate(null,scenarioURI, context);
      return (Focus) scenarioContainer.getFocus();

    }
    catch (ContextualException x)
    { throw new BindException("Error binding task method",x);
    }


  }
  
       
}
  


