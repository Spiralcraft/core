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
package spiralcraft.tuple.spi;

import spiralcraft.tuple.Type;
import spiralcraft.tuple.Scheme;

public class TypeImpl
  implements Type
{
  private Scheme _scheme;
  private Class _javaClass;
  
  public TypeImpl()
  {
  }
  
  /**
   * Copy constructor
   */
  public TypeImpl(Type type)
  { 
    _scheme=type.getScheme();
    _javaClass=type.getJavaClass();
  }
  
  public Scheme getScheme()
  { return _scheme;
  }

  public void setScheme(Scheme val)
  { _scheme=val;
  }
  
  public Class<?> getJavaClass()
  { return _javaClass;
  }
  
  public void setJavaClass(Class clazz)
  { _javaClass=clazz;
  }
}
