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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;



/**
 * <p>A Scenario which conditionally performs a scenario chain
 * </p>
 * 
 * @author mike

 */
public class If
  extends Chain
{
  
  private Channel<Boolean> channel;
  private Expression<Boolean> x;
  
  @Override
  public IfTask task()
  { return new IfTask();
  }
  
  /**
   * Provide an expression to resolve the Command object
   */
  public void setX(Expression<Boolean> x)
  { this.x=x;
  }
  
  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  { 
    channel=focusChain.bind(x);
    super.bindChildren(focusChain);
  }
  

  public class IfTask
    extends ChainTask
  {
    @Override
    public void work()
      throws InterruptedException
    { 
      try
      {
        if (Boolean.TRUE.equals(channel.get()))
        { super.work();
        }
      }
      catch (Exception x)
      { 
        addException(x);
        return;
      }
    }

  }
}