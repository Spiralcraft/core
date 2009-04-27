//
// Copyright (c) 2009,2009 Michael Toth
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

/**
 * Indicates the result of a test.
 * 
 * @author mike
 *
 */
public class TestResult
{
  private final String name;
  private final boolean passed;
  private final String message;
  private final Exception exception;
  
  public TestResult(Test<?,?> test,boolean passed)
  { 
    this.name=test.getFullyQualifiedName();
    this.passed=passed;
    this.message=null;
    this.exception=null;
  }
  
  public TestResult(Test<?,?> test,boolean passed,String message)
  { 
    this.name=test.getFullyQualifiedName();
    this.passed=passed;
    this.message=message;
    this.exception=null;
  }
  
  public TestResult(Test<?,?> test,boolean passed,String message,Exception exception)
  { 
    this.name=test.getFullyQualifiedName();
    this.passed=passed;
    this.message=message;
    this.exception=exception;
  }

  public boolean getPassed()
  { return passed;
  }
  
  public String getMessage()
  { return message;
  }
  
  public Exception getException()
  { return exception;
  }
  
  public String getName()
  { return name;
  }
  
  @Override
  public String toString()
  { 
    return (passed?"PASS":"FAIL!!!")
      +": "+name+": "+(message!=null?message+": ":"")
      +(exception!=null?exception:"");
  }
}
