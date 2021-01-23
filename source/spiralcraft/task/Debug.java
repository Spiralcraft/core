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

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;

/**
 * Conditionally evaluates an Expression and logs the result
 *  
 * @author mike
 *
 */
public class Debug<Tcontext>
  extends Scenario<Tcontext,String>
{

  protected Binding<Object> messageX;
  protected Binding<Boolean> conditionX;
  
  {
    storeResults=true;
    resultReflector=BeanReflector.<String>getInstance(String.class);
  }
  
  public void setMessageX(Binding<Object> messageX)
  { this.messageX=messageX;
  }
  
  public void setConditionX(Binding<Boolean> conditionX)
  { this.conditionX=conditionX;
  }
  
  @Override
  protected Task task()
  {
    return new AbstractTask()
    {

      @Override
      protected void work()
        throws InterruptedException
      { 
        if (debug)
        { log.log(Level.FINE,this+": executing");
        }
        Boolean condition=conditionX!=null?conditionX.get():true;
        if (messageX!=null && Boolean.TRUE.equals(condition))
        {
          Object message=messageX.get();
          String messageString;
          if (message!=null && message.getClass().isArray())
          { 
            messageString=""+ArrayUtil.format(message,",","");
          }
          else
          { 
            messageString=""+message;
          }
          log.debug(messageX.getText()+" := ["+messageString+"]");
          addResult(messageString);
        }

      }
    };    
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  {  
    if (messageX!=null)
    { messageX.bind(focusChain);
    }
    if (conditionX!=null)
    { conditionX.bind(focusChain);
    }
    super.bindChildren(focusChain);
  }
}
