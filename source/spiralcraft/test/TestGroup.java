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
package spiralcraft.test;

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Task;

/**
 * Provides for the generation of a TestResult inside another arbitrary
 *   scenario.
 * 
 * @author mike
 *
 */
public class TestGroup
  extends Test
{

  public static TestGroup find(Focus<?> focus)
    throws BindException
  { 
  
    Focus<TestGroup> testFocus
      =focus.<TestGroup>findFocus(URI.create("class:/spiralcraft/test/TestGroup"));
    if (testFocus==null)
    { 
      throw new BindException
        ("Not in focus chain: class:/spiralcraft/test/TestGroup");
    }
    return testFocus.getSubject().get();
  }
  
  protected Expression<Object> messageX;
  protected Channel<Object> messageChannel;
  
  { setLogTaskResults(true);
  }
  
  public void setMessageX(Expression<Object> messageX)
  { this.messageX=messageX;
  }
  
  
  @Override
  protected Task task()
  {
    return new AbstractTask()
    {

      @Override
      protected void work()
      { 
        if (debug)
        { log.log(Level.FINE,this+": executing");
        }
        Object message=messageChannel!=null?messageChannel.get():null;
        addResult
          (new TestResult
             (TestGroup.this
             ,Boolean.TRUE
             ,message!=null?message.toString():null
             )
          );

      }
    };    
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  {  
    if (messageX!=null)
    { messageChannel=focusChain.bind(messageX);
    }
    super.bindChildren(focusChain);
  }
}
