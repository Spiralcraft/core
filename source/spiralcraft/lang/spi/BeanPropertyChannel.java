package spiralcraft.lang.spi;

import java.beans.PropertyDescriptor;
import java.beans.PropertyChangeSupport;
import java.beans.EventSetDescriptor;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

public class BeanPropertyChannel<T,S>
  extends TranslatorChannel<T,S>
{
  private static final Object[] EMPTY_PARAMS=new Object[0];
  private final Object[] _params=new Object[1];
  private final PropertyDescriptor _property;
  private final Method _readMethod;
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
    _readMethod=_property.getReadMethod();


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
      (_property.getWriteMethod()==null
        && _propertyChangeEventSetDescriptor==null
        && isSourceStatic()
      );
      
  }

  @Override
  public boolean isStatic()
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
  public synchronized boolean set(Object val)
    throws AccessException
  {
    if (_static)
    { return false;
    }

    Method method=_property.getWriteMethod();
    Object target=getSourceValue();
    try
    {
      Object oldValue=null;
      if (_readMethod!=null)
      {
        oldValue
          =_readMethod.invoke(target,EMPTY_PARAMS);
      }
      
      if (oldValue!=val || _readMethod==null)
      { 
        _params[0]=val;
        method.invoke(target,_params);
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
      +":[property="+_property.getName()+" ("+_property.getPropertyType()+")]";
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


