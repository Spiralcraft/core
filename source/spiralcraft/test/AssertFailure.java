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

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.Task;
import spiralcraft.util.ArrayUtil;
import spiralcraft.task.Chain;

/**
 * Ensures that the chained task throws an exception
 * 
 * @author mike
 *
 */
public class AssertFailure
  extends Chain<Void,Void>
{
  private String name;
  protected TestGroup testGroup;
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    testGroup=TestGroup.find(focusChain);
    if (testGroup==null)
    { setLogTaskResults(true);
    }
    return super.bind(focusChain);
  }
  
  public void setName(String name)
  { this.name=name;
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
        try
        {
          super.work();
          if (command.getException()==null)
          {
            addException
              (new TestFailedException
                (new TestResult(name,false,"No exception generated")));
        
          }
          else
          {
            this.exception=null;
            TestResult result=new TestResult
                (name
                ,true
                ,"Exception Successfully Generated"
                );  
            if (testGroup!=null)
            { testGroup.addTestResult(result);
            }
          }
          if (debug)
          { log.log(Level.FINE,this+": executing");
          }
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
          addException(new TestFailedException(result,x));
          return;
          
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

}