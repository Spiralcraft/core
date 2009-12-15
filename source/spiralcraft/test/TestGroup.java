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
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Chain;
import spiralcraft.task.Task;

/**
 * Provides for the generation of a TestResult inside another arbitrary
 *   scenario.
 * 
 * @author mike
 *
 */
public class TestGroup
  extends Chain<Void,TestResult>
{

  public static TestGroup find(Focus<?> focus)
  { 
  
    Focus<TestGroup> testFocus
      =focus.<TestGroup>findFocus(URI.create("class:/spiralcraft/test/TestGroup"));
    if (testFocus==null)
    { return null;
    }
    return testFocus.getSubject().get();
  }
  
  protected String name;
  protected Expression<Object> messageX;
  protected Channel<Object> messageChannel;
  protected ThreadLocalChannel<List<TestResult>> resultChannel;
  protected boolean throwFailure;
  protected TestGroup testGroup;
  
  public void setMessageX(Expression<Object> messageX)
  { this.messageX=messageX;
  }
  
  public void setName(String name)
  { this.name=name;
  }
  
  public void setThrowFailure(boolean throwFailure)
  { this.throwFailure=throwFailure;
  }  
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    testGroup=TestGroup.find(focusChain);
    if (testGroup==null)
    { setLogTaskResults(true);
    }
    return super.bind(focusChain);
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
        if (debug)
        { log.log(Level.FINE,this+": executing");
        }
        
        List<TestResult> results=new LinkedList<TestResult>();
        resultChannel.push(results);
        try
        {
          super.work();
          boolean passed=true;
          int count=0;
          int failCount=0;
          for (TestResult result:results)
          {
            count++;
            if (!result.getPassed())
            { 
              passed=false;
              failCount++;
            }
          }
          Object message=messageChannel!=null?messageChannel.get():null;
          TestResult result
            =new TestResult
               (name
               , passed
               ,(message!=null?message.toString()+": ":"")
               +count+" results, "+failCount+" failures"
               ,results
               );
          if (testGroup!=null)
          { testGroup.addTestResult(result);
          }
          if (throwFailure && !passed)
          { addException(new TestFailedException(result));
          }          
          addResult(result);
          
        }
        finally
        { resultChannel.pop();
        }

      }
    };    
  }

  public void addTestResult(TestResult result)
  { 
    final List<TestResult> results=resultChannel.get();
    synchronized (results)
    { results.add(result);
    }
  }
  
  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  {  
    resultChannel
      =new ThreadLocalChannel<List<TestResult>>
        (BeanReflector.<List<TestResult>>getInstance(List.class),true);
    
    focusChain=focusChain.chain(resultChannel);
    if (messageX!=null)
    { messageChannel=focusChain.bind(messageX);
    }
    super.bindChildren(focusChain);
  }
}



