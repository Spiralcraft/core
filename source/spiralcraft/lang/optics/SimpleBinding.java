package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

import java.beans.PropertyChangeSupport;

/**
 * An Binding bound to a self-contained Object 
 */
public class SimpleBinding
  implements Binding
{
 
  private Class _class;
  private Object _object;
  private PropertyChangeSupport _propertyChangeSupport;
  private final boolean _static;

  /**
   * Create a SimpleOptic with the specified Object as its target
   *   and with a targetClass equals to the Object's class.
   */
  public SimpleBinding(Object val,boolean isStatic)
  { 
    _object=val;
    _class=_object.getClass();
    _static=isStatic;
  }

  public SimpleBinding(Class clazz,Object val,boolean isStatic)
  { 
    _object=val;
    _class=clazz;
    _static=isStatic;
  }

  public Object get()
  { return _object;
  }

  public synchronized boolean set(Object value)
  { 
    if (_static)
    { return false;
    }
    else
    {
      Object oldValue=_object;
      _object=value;
      
      if (_propertyChangeSupport!=null)
      { _propertyChangeSupport.firePropertyChange("",oldValue,value);
      }
      return true;
    }
  }

  public Class getTargetClass()
  { return _class;
  }

  public boolean isStatic()
  { return _static;
  }

  public synchronized PropertyChangeSupport propertyChangeSupport()
  { 
    if (_static)
    { return null;
    }
    else if (_propertyChangeSupport==null)
    { _propertyChangeSupport=new PropertyChangeSupport(this);
    }
    return _propertyChangeSupport;
  }
}
