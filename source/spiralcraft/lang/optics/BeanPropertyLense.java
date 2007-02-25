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

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.lang.BindException;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Optic;

/**
 * A lense associated with a single bean property. The 'get' transformation
 *   simply retrieves the value of the property from the supplied bean.
 */
class BeanPropertyLense<Tprop,Tbean>
  implements Lense<Tprop,Tbean>
{
  private static final Object[] EMPTY_PARAMS=new Object[0];

  private final PropertyDescriptor _property;
  private final Method _readMethod;
  private final MappedBeanInfo _beanInfo;
  private final Prism<Tprop> _prism;
  
  @SuppressWarnings("unchecked") // PropertyDescriptor is not generic
  public BeanPropertyLense(PropertyDescriptor property,MappedBeanInfo beanInfo)
    throws BindException
  { 
    _property=property;
    _readMethod=property.getReadMethod();
    _beanInfo=beanInfo;
    _prism=OpticFactory.getInstance().<Tprop>findPrism
      ((Class<Tprop>)_property.getPropertyType());
    
  }

  public MappedBeanInfo getBeanInfo()
  { return _beanInfo;
  }
  
  public PropertyDescriptor getProperty()
  { return _property;
  }

  @SuppressWarnings("unchecked") // Method is not generic
  public Tprop translateForGet(Tbean value,Optic[] modifiers)
  { 
    try
    {
      if (_readMethod!=null)
      { 
        if (value!=null)
        { return (Tprop) _readMethod.invoke(value,EMPTY_PARAMS);
        }
        else
        { return null;
        }
      }
      else
      { 
        System.err.println
          ("Cannot read property '"
          +value.getClass().getName()
          +"."+_property.getName()+"'"
          );
        return null;
      }
    }
    catch (IllegalAccessException x)
    { 
      x.printStackTrace();
      return null;
    }
    catch (InvocationTargetException x)
    { 
      x.getTargetException().printStackTrace();
      return null;
    }
  }

  public Tbean translateForSet(Tprop val,Optic[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Prism<Tprop> getPrism()
  { return _prism;
  }

}

