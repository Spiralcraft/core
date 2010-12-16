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

import spiralcraft.data.DataException;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.AssignmentChannel;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;

import spiralcraft.task.Scenario;
import spiralcraft.task.TaskCommand;

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
  

  
  @Override
  protected Focus<?> bindTask(
    Focus<?> context,
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  {
    
    if (scenarioURI==null)
    { 
      scenarioURI
        =URI.create
          (getDataType().getURI().toString()+"_"+getName()+".scenario.xml");
    }
    
    AbstractXmlObject<Scenario<C,R>,?> scenarioContainer
      =AbstractXmlObject
        .<Scenario<C,R>>activate(null,scenarioURI, context);

    return scenarioContainer.getFocus();

  }
  
       
}
  


