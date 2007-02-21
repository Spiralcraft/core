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

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;


/**
 * An Binding bound to a self-contained Object 
 */
public class SimpleBinding
  extends AbstractBinding
{
 
  private Object _object;
  
  /**
   * Create a SimpleOptic with the specified Object as its target
   *   and with a targetClass equals to the Object's class.
   */
  public SimpleBinding(Object val,boolean isStatic)
    throws BindException
  { 
    super(OpticFactory.getInstance().findPrism(val.getClass()),isStatic);
    _object=val;
  }

  public SimpleBinding(Class clazz,Object val,boolean isStatic)
    throws BindException
  { 
    super(OpticFactory.getInstance().findPrism(clazz),isStatic);
    _object=val;
  }

  
  protected Object retrieve()
  { return _object;
  }
  
  protected boolean store(Object val)
  { 
    _object=val;
    return true;
  }

}
