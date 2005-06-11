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

/**
 * A Focus where the subject is a java Bean
 */
public class BeanFocus
  extends DefaultFocus
{
  
  public BeanFocus()
  {
  }
  
  public BeanFocus(Object bean)
    throws BindException
  { setBean(bean);
  }
  
  public void setBean(Object bean)
    throws BindException
  { setSubject(OpticFactory.getInstance().createOptic(bean));
  }
}
