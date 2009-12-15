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
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.AbstractChannel;

import spiralcraft.task.Scenario;
import spiralcraft.task.TaskCommand;

public class TaskMethod<T,C,R>
  extends MethodImpl
{

  private URI scenarioURI;
  
  public void setScenarioURI(URI scenarioURI)
  { this.scenarioURI=scenarioURI;
  }
  
  @Override
  public void subclassResolve()
    throws DataException
  { 
    if (this.returnType==null)
    { this.returnType=ReflectionType.canonicalType(TaskCommand.class);
    }
  }
  
  @Override
  public Channel<?> bind(
    Focus<?> focus,
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  {
    
    if (focus.getSubject()!=source)
    { focus=focus.chain(source);
    }
    
    if (scenarioURI==null)
    { 
      scenarioURI
        =URI.create
          (getDataType().getURI().toString()+"_"+getName()+".scenario.xml");
    }
    
    AbstractXmlObject<Scenario<C,R>,?> scenarioContainer
      =AbstractXmlObject
        .<Scenario<C,R>>activate(null,scenarioURI, null, focus);

    focus=scenarioContainer.getFocus();
    
    
    Scenario<C,R> scenario=scenarioContainer.get();
    
    return new TaskMethodChannel(scenario);
    
    

  }
  
  public class TaskMethodChannel
    extends AbstractChannel<TaskCommand<C,R>>
  {
    private final Scenario<C,R> scenario;
    
    public TaskMethodChannel(Scenario<C,R> scenario)
    { 
      super(scenario.getCommandReflector());
      this.scenario=scenario;
    }
    
    @Override
    protected TaskCommand<C,R> retrieve()
    { 
      
      TaskCommand<C,R> command=scenario.command();
      command.encloseContext();
      return command;
    }

    @Override
    protected boolean store(
      TaskCommand<C,R> val)
      throws AccessException
    { return false;
    }
  }

}
