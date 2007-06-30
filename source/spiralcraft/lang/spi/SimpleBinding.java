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
import spiralcraft.lang.Reflector;


/**
 * An Binding which references a self-contained Object. Used to provide a
 *   programatically defined target against which Expressions can be evaluated.
 */
public class SimpleBinding<T>
  extends AbstractBinding<T>
{
 
  private T _object;
  
  /**
   * Create a SimpleOptic with the specified Object as its target
   *   and with a targetClass equals to the Object's class.
   */
  @SuppressWarnings("unchecked") // does not return properly scoped class
  public SimpleBinding(T val,boolean isStatic)
    throws BindException
  { 
    super(BeanReflector.<T>getInstance((Class<T>) val.getClass()),isStatic);
    _object=val;

    // System.out.println("SimpleBinding- noclass:"+super.toString()+":["+val+"]");
  }

  public SimpleBinding(Class<T> clazz,T val,boolean isStatic)
    throws BindException
  { 
     
    super(BeanReflector.getInstance(clazz),isStatic);
    _object=val; 

    //System.out.println("SimpleBinding- with class:"+super.toString()+":["+val+"]");
  }

  public SimpleBinding(Reflector<T> reflector,T val,boolean isStatic)
  { 
    super(reflector,isStatic);
    _object=val;
  }
  
  protected T retrieve()
  { 
    // System.out.println("SimpleBinding "+super.toString()+" - returning "+_object);

    return _object;
  }
  
  protected boolean store(T val)
  { 
    _object=val;
    return true;
  }

}
