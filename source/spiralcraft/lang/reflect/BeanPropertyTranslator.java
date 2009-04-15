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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * A Translator associated with a single bean property. The 'get' transformation
 *   simply retrieves the value of the property from the supplied bean.
 */
class BeanPropertyTranslator<Tprop,Tbean>
  implements Translator<Tprop,Tbean>
{
  private static final Object[] EMPTY_PARAMS=new Object[0];
  
  private static final ClassLog log
    =ClassLog.getInstance(BeanPropertyTranslator.class);
  
  private static Level debugLevel
    =ClassLog.getInitialDebugLevel(BeanPropertyTranslator.class,null);

  private final PropertyDescriptor _property;
  private final Method _readMethod;
  private final MappedBeanInfo _beanInfo;
  private final Reflector<Tprop> _reflector;
  private final Field _publicField;
  
  @SuppressWarnings("unchecked") // PropertyDescriptor is not generic
  public BeanPropertyTranslator
    (PropertyDescriptor property
    ,MappedBeanInfo beanInfo
    )
  { 
    
    _property=property;
    _readMethod=beanInfo.getCovariantReadMethod(property);
    _beanInfo=beanInfo;
    
    Class beanClass=beanInfo.getBeanDescriptor().getBeanClass();
    
    
    
    Reflector<Tprop> reflector;
    
    if (_readMethod!=null)
    {
      Type genericType=_readMethod.getGenericReturnType();
      if (!(genericType instanceof TypeVariable))
      {
        reflector=BeanReflector.<Tprop>getInstance(genericType);
      }
      else
      {
        reflector
          =BeanReflector.<Tprop>getInstance(_readMethod.getReturnType());
      }
    }
    else
    {
      reflector=BeanReflector.<Tprop>getInstance
        ((Class<Tprop>)_property.getPropertyType());
    }

    
    Field field=null;
    try
    {
      field
        =beanClass
          .getField(_property.getName());
      if (!Modifier.isPublic(field.getModifiers()))
      { field=null;
      }
      else if 
        (!reflector.getContentType()
          .isAssignableFrom(field.getType())
        && !field.getType().isAssignableFrom(reflector.getContentType())
        )
      { 
        if (Level.DEBUG.canLog(debugLevel))
        {
          log.debug("Class member field "+field.toString()
                  +" is not type compatible with property "
                 +beanClass.getName()+"."+_property.getName()+" ("
                 +reflector.getContentType().getName()+")"
                 );
        }
        field=null;
      }
        
    }
    catch (NoSuchFieldException x)
    {  
    }
    _publicField=field;
    
    
    if (_property.getWriteMethod()==null 
        && _publicField!=null
       )
    {
      if (_readMethod==null 
         || _readMethod.getReturnType().isAssignableFrom(_publicField.getType())
         )
      { 
        // Property is writable through a public field that is typed 
        //   more specifically than the read method. We need to
        //   make the property type more specific.
        reflector
          =BeanReflector.<Tprop>getInstance(_publicField.getType());
        
        if (Level.DEBUG.canLog(debugLevel))
        {
          log.debug
            ("Property "+beanClass.getName()+"."+property.getName()
            +" narrowed to type "+_publicField.getType().getName()
            +" due to existence of public field setter"
            );
        }
      }
    }
    
    _reflector=reflector;

  }

  public Field getPublicField()
  { return _publicField;
  }
  
  public Method getReadMethod()
  { return _readMethod;
  }
  
  public MappedBeanInfo getSourceBeanInfo()
  { return _beanInfo;
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
      else if (_publicField!=null)
      { 
        if (value!=null)
        { return (Tprop) _publicField.get(value);
        }
        else
        { return null;
        }
      }
      else
      { 
        if (Level.TRACE.canLog(debugLevel))
        {
          log.debug
            ("No read method or public field for '"
            +value.getClass().getName()
            +"."+_property.getName()+"'"
            );
        }
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

