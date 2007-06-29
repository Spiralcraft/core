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
package spiralcraft.lang.optics;

import spiralcraft.lang.BindException;

/**
 * <P>Associates values with Attributes defined in a NamespacePrism
 *   
 * <P>This permits applications to dynamically define namespaces and bind
 *   Expressions to them.
 */
public class Namespace
{

  private NamespacePrism prism;
  private Object[] data;
  
  public Namespace(NamespacePrism prism)
  { 
    this.prism=prism;
    this.data=new Object[prism.getAttributeCount()];
  }
  
  Object get(int index)
  { return data[index];
  }
  
  void set(int index,Object value)
  { data[index]=value;
  }
  
  public void put(String name,Object value)
    throws BindException
  { prism.setValue(this,name,value);
  }
  
  public Object get(String name)
    throws BindException
  { return prism.getValue(this,name);
  }
  
}


