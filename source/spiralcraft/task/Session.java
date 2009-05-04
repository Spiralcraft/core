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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Provides a stateful typed Context that can be shared between scenarios
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
  
  public void setType(Type<? extends DataComposite> type)
  { this.type=type;
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
    dataSessionFocus
      =new DataSessionFocus(focusChain,sessionChannel,type);
    super.bindChildren(dataSessionFocus);
  }

}
