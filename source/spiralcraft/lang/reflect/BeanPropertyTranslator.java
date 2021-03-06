//
// Copyright (c) 1998,2009 Michael Toth
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

import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.lang.ClassUtil;

/**
 * A Translator associated with a single bean property. The 'get' transformation
 *   simply retrieves the value of the property from the supplied bean.
 */
public class BeanPropertyTranslator<Tprop,Tbean>
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
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // PropertyDescriptor is not generic
  public BeanPropertyTranslator
    (PropertyDescriptor property
    ,MappedBeanInfo beanInfo
    )
  { 
    
    _property=property;
    _readMethod=beanInfo.getCovariantReadMethod(property);
    _beanInfo=beanInfo;
    
    Class beanClass=beanInfo.getBeanDescriptor().getBeanClass();
    
    Class<Tprop> propertyType=(Class<Tprop>) _property.getPropertyType();
    if (propertyType==null)
    { 
      throw new IllegalArgumentException
        ("Property '"+_property.getName()+"' must have a type"); 
    }
    
    Reflector<Tprop> reflector
      =BeanReflector.<Tprop>getInstance
        ((Class<Tprop>)_property.getPropertyType());
    
    if (_readMethod!=null)
    {
      Class genericType=ClassUtil.getClass(_readMethod.getGenericReturnType());
      if (genericType!=null
          && reflector.getContentType().isAssignableFrom(genericType))
      {
        // Generic type is more specific
        reflector=BeanReflector.<Tprop>getInstance(genericType);
      }
      else
      {
        if (reflector.getContentType().isAssignableFrom
          (_readMethod.getReturnType()))
        {
          // Read method actual return type is more specific 
          reflector
            =BeanReflector.<Tprop>getInstance(_readMethod.getReturnType());
        }
      }
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
        if (debugLevel.canLog(Level.DEBUG))
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
        if (reflector.getContentType().isAssignableFrom
              (_publicField.getType())
           )
        {
          
          reflector
            =BeanReflector.<Tprop>getInstance(_publicField.getType());
        
          if (debugLevel.canLog(Level.DEBUG))
          {
            log.debug
              ("Property "+beanClass.getName()+"."+property.getName()
              +" narrowed to type "+_publicField.getType().getName()
              +" due to existence of public field setter"
              );
          }
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

  @Override
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
        if (debugLevel.canLog(Level.TRACE))
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

  @Override
  public Tbean translateForSet(Tprop val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }

  /**
   * Properties are mutable
   */
  @Override
  public boolean isFunction()
  { 
    // TODO: Provide for annotation for properties immutable after binding
    return false;
  }  
  
  @Override
  public Reflector<Tprop> getReflector()
  { return _reflector;
  }


}

