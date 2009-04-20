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
import java.beans.PropertyChangeSupport;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.TranslatorChannel;

class BeanPropertyChannel<T,S>
  extends TranslatorChannel<T,S>
{
  private static final Object[] EMPTY_PARAMS=new Object[0];
  private final Object[] _params=new Object[1];
  private final PropertyDescriptor _property;
  private final Method _readMethod;
  private final Method _writeMethod;
  private final Field _writeField;
  private final boolean _static;
  private final EventSetDescriptor _propertyChangeEventSetDescriptor;
  private final Object[] _beanPropertyChangeListenerParams;

  public BeanPropertyChannel
    (Channel<S> source
    ,BeanPropertyTranslator<T,S> translator
    )
  {
    super(source,translator,null);
    _property=translator.getProperty();
    _readMethod=translator.getReadMethod();
    
    if (_property.getWriteMethod()!=null)
    { _writeMethod=_property.getWriteMethod();
    }
    else
    { 
      // Expensive, but may be required
      _writeMethod
        =translator.getSourceBeanInfo()
          .getSpecificWriteMethod
            (_property
            ,translator.getReflector().getContentType()
            );
    }

    _writeField
      =_writeMethod==null
      ?translator.getPublicField()
      :null;
    

    _propertyChangeEventSetDescriptor
      =translator.getBeanInfo().getPropertyChangeEventSetDescriptor();

    if (_propertyChangeEventSetDescriptor!=null)
    { 
      _beanPropertyChangeListenerParams
        =new Object[] {new BeanPropertyChangeListener()};
    }
    else
    { _beanPropertyChangeListenerParams=null;
    }

    _static=
      (_writeMethod==null && _writeField==null
        && _propertyChangeEventSetDescriptor==null
        && isSourceConstant()
      );
      
  }

  @Override
  public boolean isConstant()
  { return _static;
  }

  @Override
  public PropertyChangeSupport propertyChangeSupport()
  {
    // Install a propertyChangeListener for the specific property we are watching in
    //   addition to the one that listens for changes to the source object reference
    
    PropertyChangeSupport support;
    if (!isPropertyChangeSupportActive())
    {
      support=super.propertyChangeSupport();
      if (support!=null && _beanPropertyChangeListenerParams!=null)
      { 
        Object target=getSourceValue();
        try
        {
          if (target!=null)
          { 
            _propertyChangeEventSetDescriptor.getAddListenerMethod().invoke
              (target
              ,_beanPropertyChangeListenerParams
              );
          }
        }
        catch (IllegalAccessException x)
        { x.printStackTrace();
        }
        catch (InvocationTargetException x)
        { x.getTargetException().printStackTrace();
        }
      }
    }
    else
    { support=super.propertyChangeSupport();
    }
    return support;
  }
  
  /**
   * Extend behavior to transfer the beanPropertyChangeListener over
   *   from the old source value to the new source value
   */
  public void propertyChanged(PropertyChangeEvent event)
  { 
    if (_beanPropertyChangeListenerParams!=null)
    { 
      try
      {
        if (event.getOldValue()!=null)
        { 
          _propertyChangeEventSetDescriptor.getRemoveListenerMethod().invoke
            (event.getOldValue()
            ,_beanPropertyChangeListenerParams
            );
        }
        super.propertyChange(event);
        if (event.getNewValue()!=null)
        { 
          _propertyChangeEventSetDescriptor.getAddListenerMethod().invoke
            (event.getNewValue()
            ,_beanPropertyChangeListenerParams
            );
        }
      }
      catch (IllegalAccessException x)
      { x.printStackTrace();
      }
      catch (InvocationTargetException x)
      { x.getTargetException().printStackTrace();
      }
    }
    else
    { super.propertyChange(event);
    }
  }
  
  @Override
  public boolean isWritable()
  {
    if (_static)
    { return false;
    }
    
    if (_writeMethod==null && _writeField==null)
    { return false;
    }
    
    return true;
  }
  
  
  @Override
  public synchronized boolean set(Object val)
    throws AccessException
  {
    if (_static)
    { return false;
    }

    if (_writeMethod==null && _writeField==null)
    { return false;
    }
    
    Object target=getSourceValue();
    
    if (target==null)
    { return false;
    }
    
    try
    {
      if (isPropertyChangeSupportActive())
      {
        Object oldValue=null;
        if (_readMethod!=null)
        {
          oldValue
            =_readMethod.invoke(target,EMPTY_PARAMS);
        }
      
        if (oldValue!=val || _readMethod==null)
        { 
          if (_writeMethod!=null)
          { 
            _params[0]=val;
            _writeMethod.invoke(target,_params);
          }
          else
          { _writeField.set(target,val);
          }
          // System.out.println(toString()+".set: "+val);
          if (_propertyChangeEventSetDescriptor==null)
          { 
            // Only fire propertyChange if the bean has no facility 
            //   to do so itself
            firePropertyChange(_property.getName(),oldValue,val);
          }
          return true;
        }
        else
        { return false;
        }
      }
      else
      {

        // Don't compare values if we're not tracking property changes
        if (_writeMethod!=null)
        {
          _params[0]=val;
          _writeMethod.invoke(target,_params);

        }
        else
        { _writeField.set(target,val);
        }
        return true;
      }
    }
    catch (RuntimeException x)
    {
      throw new AccessException
        (x.toString()+" writing bean property '"+_property.getName()+"':"
        ,x);
    }
    catch (IllegalAccessException x)
    { 
      throw new AccessException
        (x.toString()+" writing bean property '"+_property.getName()+"'",x);
    }
    catch (InvocationTargetException x)
    { 
      throw new AccessException
        (x.toString()+":"+x.getTargetException().toString()
        +" writing bean property '"+_property.getName()+"'"
        ,x);
    }
  }


  @Override
  public String toString()
  { 
    return super.toString()
      +":[property="
      +_property.getName()+" ("+_property.getPropertyType()+")" 
      +" getter="+_readMethod
      +(_writeField!=null
          ?" setterField="+_writeField
          :" setter="+_writeMethod
       )
      +"]";
  }
  
  class BeanPropertyChangeListener
    implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent event)
    {
      if (event.getPropertyName().equals(_property.getName()))
      { firePropertyChange(event);
      }
    }    
  }
}


