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

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;
//import spiralcraft.log.ClassLog;

/**
 * A Translator associated with a single bean property. The 'get' transformation
 *   simply retrieves the value of the property from the supplied bean.
 */
class BeanPropertyTranslator<Tprop,Tbean>
  implements Translator<Tprop,Tbean>
{
  private static final Object[] EMPTY_PARAMS=new Object[0];
  
//  private static final ClassLog log
//    =ClassLog.getInstance(BeanPropertyTranslator.class);

  private final PropertyDescriptor _property;
  private final Method _readMethod;
  private final MappedBeanInfo _beanInfo;
  private final Reflector<Tprop> _reflector;
  
  @SuppressWarnings("unchecked") // PropertyDescriptor is not generic
  public BeanPropertyTranslator(PropertyDescriptor property,MappedBeanInfo beanInfo)
  { 
    
    _property=property;
    _readMethod=beanInfo.getCovariantReadMethod(property);
    
    _beanInfo=beanInfo;
    if (_readMethod!=null)
    {
      _reflector=BeanReflector.<Tprop>getInstance
        (_readMethod.getGenericReturnType());
    }
    else
    {
      _reflector=BeanReflector.<Tprop>getInstance
        ((Class<Tprop>)_property.getPropertyType());
    }
//    if (_property.getName().equals("state"))
//    { 
//      if (_readMethod!=null)
//      {
//        log.fine("State "
//                +_property.getReadMethod().getReturnType()+"\r\n"
//                +_property.getReadMethod().getGenericReturnType()+"\r\n"
//                +_property.getPropertyType()+"\r\n"
//              );
//      }
//      log.fine(beanInfo.getBeanDescriptor().getBeanClass().toString());
//    }
  }

  public MappedBeanInfo getBeanInfo()
  { return _beanInfo;
  }
  
  public PropertyDescriptor getProperty()
  { return _property;
  }

  @SuppressWarnings("unchecked") // Method is not generic
  public Tprop translateForGet(Tbean value,Channel<?>[] modifiers)
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
//        System.err.println
//          ("BeanPropertyTranslator: No read method for '"
//          +value.getClass().getName()
//          +"."+_property.getName()+"'"
//          );
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
    catch (IllegalArgumentException x)
    { 
      throw new IllegalArgumentException
        ("Invoking method "+_readMethod+" on "+value,x);
    }
  }

  public Tbean translateForSet(Tprop val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Reflector<Tprop> getReflector()
  { return _reflector;
  }

}

