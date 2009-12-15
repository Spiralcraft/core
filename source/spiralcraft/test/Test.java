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
package spiralcraft.test;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.task.Scenario;

public abstract class Test
  extends Scenario<Void,TestResult>
{

  protected TestGroup testGroup;
  
  
  protected String name;
  protected boolean throwFailure;
  
  /**
   * The fully qualified name of the test, in order to identify results
   * 
   * @param name
   */
  public void setName(String name)
  { this.name=name;
  }
  
  public String getName()
  { return this.name;
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

}
