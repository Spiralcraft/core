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
package spiralcraft.lang.spi;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

/**
 * <P>Associates values with Attributes defined in a NamespaceReflector
 *   
 * <P>This permits applications to dynamically define namespaces and bind
 *   Expressions to them.
 */
public class Namespace
{

  private NamespaceReflector reflector;
  private Channel<?>[] data;
  
  public Namespace(NamespaceReflector reflector)
  { 
    this.reflector=reflector;
    this.data=new Channel[reflector.getAttributeCount()];
  }
  
  Channel<?> getOptic(int index)
  { return data[index];
  }
  
  void setOptic(int index,Channel<?> channel)
  { data[index]=channel;
  }
  
  public void putOptic(String name,Channel<?> value)
    throws BindException
  { reflector.putOptic(this,name,value);
  }
  
  public Object getOptic(String name)
    throws BindException
  { return reflector.getOptic(this,name);
  }
  
  public String toString()
  { return super.toString()+"{"+reflector.toString()+"}";
  }
  
  
}


