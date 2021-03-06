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
package spiralcraft.lang.reflect;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * A Focus where the subject is a java Bean
 */
public class BeanFocus<T>
  extends SimpleFocus<T>
{
  
  SimpleChannel<T> binding;
  
  /**
   * Publish a channel containing the specified initial value into the focus 
   *   chain
   * 
   * @param clazz
   * @param bean
   */
  public BeanFocus(Class<T> clazz,T bean)
  { 
    binding=new SimpleChannel<T>(clazz,bean,false);
    setSubject(binding);
  }
  
  @SuppressWarnings("unchecked") // Stupid cast 
  
  /**
   * Publish a constant channel containing the bean into the focus chain
   */
  public BeanFocus(T bean)
  { 
    binding=new SimpleChannel<T>( (Class<T>) bean.getClass(),bean,true);
    setBean(bean);
    setSubject(binding);
  }
  
  public void setBean(T bean)
  { 
    try
    { binding.set(bean);
    }
    catch (AccessException x)
    {
    }
  }
}
