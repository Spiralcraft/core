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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

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
  private final List<TestResult> results;
  
  public TestResult(Test test,boolean passed)
  { 
    this.name=test.getName();
    this.passed=passed;
    this.message=null;
    this.exception=null;
    this.results=null;
  }
  
  public TestResult(Test test,boolean passed,String message)
  { 
    this.name=test.getName();
    this.passed=passed;
    this.message=message;
    this.exception=null;
    this.results=null;
  }
  
  public TestResult(Test test,boolean passed,String message,Exception exception)
  { 
    this.name=test.getName();
    this.passed=passed;
    this.message=message;
    this.exception=exception;
    this.results=null;
  }

  public TestResult
    (Test test,boolean passed,String message,List<TestResult> results)
  { 
    this.name=test.getName();
    this.passed=passed;
    this.message=message;
    this.exception=null;
    this.results=results;
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
    StringWriter writer=new StringWriter();
    try
    { format(writer,"  ");
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    return "\r\n  "+writer.toString();
  }
  
  public void format(Writer writer,String prefix)
    throws IOException
  {
    writer.write(
      (passed?"PASS":"FAIL!!!")
      +(name!=null?": "+name:"")+": "+(message!=null?message+": ":"")
      +(exception!=null?exception:"")
      );
    
    if (results!=null)
    {
      String subPrefix=prefix+"  ";
      for (TestResult result:results)
      { 
        writer.write("\r\n"+subPrefix);
        result.format(writer, subPrefix);
      }
    }
  }
}
