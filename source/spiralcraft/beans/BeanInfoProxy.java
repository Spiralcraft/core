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
package spiralcraft.beans;

import java.beans.BeanInfo;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

import java.awt.Image;

/**
 * Base class for decorating BeanInfo objects with additional functionality.
 *
 * Simply wraps another object which implements the BeanInfo interface
 *   and delegates all methods by default.
 */
class BeanInfoProxy
  implements BeanInfo
{
  protected final BeanInfo beanInfo;

  public BeanInfoProxy(BeanInfo beanInfo)
  { this.beanInfo=beanInfo;
  }

  public BeanInfo[] getAdditionalBeanInfo()
  { return beanInfo.getAdditionalBeanInfo();
  }

  public BeanDescriptor getBeanDescriptor()
  { return beanInfo.getBeanDescriptor();
  }

  public int getDefaultEventIndex()
  { return beanInfo.getDefaultEventIndex();
  }
  
  public int getDefaultPropertyIndex()
  { return beanInfo.getDefaultPropertyIndex();
  }

  public EventSetDescriptor[] getEventSetDescriptors()
  { return beanInfo.getEventSetDescriptors();
  }

  public Image getIcon(int iconKind)
  { return beanInfo.getIcon(iconKind);
  }

  public MethodDescriptor[] getMethodDescriptors()
  { return beanInfo.getMethodDescriptors();
  }

  public PropertyDescriptor[] getPropertyDescriptors()
  { return beanInfo.getPropertyDescriptors();
  }
}
