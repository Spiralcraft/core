//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.exec.test;

import spiralcraft.util.ArrayUtil;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

public class HelloWorld
  implements Executable
{
  private Object _testObject;

  @Override
  public void execute(String ... args)
  { 
    ExecutionContext context=ExecutionContext.getInstance();
    
    context.out().println("HelloWorld");
    context.out().println("args: "+ArrayUtil.format(args," ","\""));
    context.out().println("testObject:");
    
    if (_testObject!=null)
    { context.out().println(_testObject.toString());
    }
  }

  public void setTestObject(Object testObject)
  { _testObject=testObject;
  }
}
