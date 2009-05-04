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


import spiralcraft.task.Scenario;

public abstract class Test
  extends Scenario
{


  
  { setLogTaskResults(true);
  }
  
  private String name;
  
  /**
   * The fully qualified name of the test, in order to identify results
   * 
   * @param name
   */
  public void setName(String name)
  { this.name=name;
  }
  
  public String getFullyQualifiedName()
  { return getClass().getName()+(name!=null?"-"+name:"");
  }

}
