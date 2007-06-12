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
package spiralcraft.lang;

import spiralcraft.lang.optics.SimpleBinding;

/**
 * A Focus where the subject is a java Bean
 */
public class BeanFocus<T>
  extends DefaultFocus<T>
{
  
  SimpleBinding<T> binding;
  
  public BeanFocus(Class<T> clazz,T bean)
    throws BindException
  { 
    binding=new SimpleBinding<T>(clazz,bean,false);
    setSubject(binding);
  }
  
  @SuppressWarnings("unchecked") // Stupid cast 
  public BeanFocus(T bean)
    throws BindException
  { 
    binding=new SimpleBinding<T>( (Class<T>) bean.getClass(),bean,true);
    setBean(bean);
    setSubject(binding);
  }
  
  public void setBean(T bean)
  { binding.set(bean);
  }
}
