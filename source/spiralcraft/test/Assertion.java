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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Task;
import spiralcraft.util.ArrayUtil;


/**
 * Evaluates a boolean test Expression against the result of an arbitrary
 *   subject expression.
 * 
 * @author mike
 *
 */
public class Assertion
  extends Test
{

  protected Expression<Object> subjectX;
  protected Channel<Object> subjectChannel;
  protected Expression<Boolean> testX;
  protected Channel<Boolean> testChannel;
  
  protected ThreadLocalChannel<Object> compareChannel;

  
  public void setSubjectX(Expression<Object> subjectX)
  { this.subjectX=subjectX;
  }
  
  public void setTestX(Expression<Boolean> testX)
  { this.testX=testX;
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
        try
        {  compareChannel.push(subjectChannel.get());
        }
        catch (RuntimeException x)
        {
          TestResult result=new TestResult
            (name
            ,false
            ,"Caught Exception"
            ,x
            );        

          if (testGroup!=null)
          { testGroup.addTestResult(result);
          }
          if (throwFailure && !result.getPassed())
          { addException(new TestFailedException(result,x));
          }
          addResult
            (result
            );
          
          return;
          
        }
        
        try
        {
          TestResult result;
        
          try
          {
            Boolean condition=testChannel.get();
            String message
              ="Result of ["+subjectX.getText()+"] is ["
                +Assertion.this.toString(compareChannel.get())+"] "
                +", testing ["+testX.getText()+"]";
          
            result=new TestResult
               (name
               ,Boolean.TRUE.equals(condition)
               ,message
               );
         
          }
          catch (RuntimeException x)
          {
            result=new TestResult
              (name
              ,false
              ,"Caught Exception"
              ,x
              );
            
          }
          
          if (testGroup!=null)
          { testGroup.addTestResult(result);
          }
          if (throwFailure && !result.getPassed())
          { addException(new TestFailedException(result,result.getException()));
          }
          addResult
            (result
            );
          
        }
        finally
        { compareChannel.pop();
        }
        
      }
    };    
  }
  
  public String toString(Object o)
  {
    if (o!=null && o.getClass().isArray())
    { return o.toString()+":["+ArrayUtil.format(o, ",", "'")+"]";
    }
    else
    { return o==null?null:o.toString();
    }
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  {  
    
    if (subjectX!=null)
    { 
      try
      { subjectChannel=focusChain.bind(subjectX);
      }        
      catch (BindException x)
      { throw new BindException
          ("Error binding subject expression ["+subjectX.getText()
            +"] in assertion '"+name+"'"
          ,x);
      }
      
      compareChannel
        =new ThreadLocalChannel<Object>(subjectChannel.getReflector());
      focusChain=focusChain.<Object>chain(compareChannel);
      
      if (testX!=null)
      { 
        try
        { testChannel=focusChain.bind(testX);
        }
        catch (BindException x)
        { throw new BindException
            ("Error binding test expression ["+testX.getText()
              +"] in assertion '"+name+"'"
            ,x);
        }
      }
      else
      { throw new BindException
          ("Test expression cannot be null in assertion '"+name+"'");
      }
    }
    else
    { throw new BindException("Subject expression cannot be null");
    }

    super.bindChildren(focusChain);
  }
}