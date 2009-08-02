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


import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.session.DataSessionFocus;
import spiralcraft.data.session.DataSession;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Provides a stateful typed DataSession Context that can be shared between 
 *   scenarios and that manages buffers for chained scenarios.
 * </p>
 *
 * @author mike
 *
 * @param <Tresult>
 */
public class Session
  extends Scenario
{

  
  protected ThreadLocalChannel<DataSession> sessionChannel
    =new ThreadLocalChannel<DataSession>
      (BeanReflector.<DataSession>getInstance(DataSession.class));
  
  protected DataSessionFocus dataSessionFocus;
  protected Type<? extends DataComposite> type;
  protected Expression<Type<? extends DataComposite>> typeX;
  protected Assignment<?>[] initialAssignments;
  protected Setter<?>[] initialSetters;
  
  public void setType(Type<? extends DataComposite> type)
  { this.type=type;
  }
  
  public void setTypeX(Expression<Type<? extends DataComposite>> typeX)
  { this.typeX=typeX;
  }

  public void setInitialAssignments(Assignment<?>[] initialAssignments)
  { this.initialAssignments=initialAssignments;
  }
  
  @Override
  protected Task task()
  {
    return new ChainTask()
    {
        
      @Override
      public void work()
        throws InterruptedException
      {
        sessionChannel.push(null);
        dataSessionFocus.reset();
        Setter.applyArray(initialSetters);
        
        super.work();
        sessionChannel.pop();
      }
    };
  }
  
  
  @Override
  protected void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    if (typeX!=null)
    { 
      Channel<Type<? extends DataComposite>> channel
        =focusChain.bind(typeX);
      if (channel!=null)
      { type=channel.get();
      }
    }
    dataSessionFocus
      =new DataSessionFocus(focusChain,sessionChannel,type);
    initialSetters=Assignment.bindArray(initialAssignments, dataSessionFocus);
    super.bindChildren(dataSessionFocus);
  }

}
