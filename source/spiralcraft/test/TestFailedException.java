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

public class TestFailedException
  extends Exception
{

  private static final long serialVersionUID = 1981392318389152266L;
  private final TestResult result;
  
  public TestFailedException(TestResult result)
  { this.result=result;
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+result.toString();
  }
}
