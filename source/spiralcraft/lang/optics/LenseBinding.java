package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * An Binding which translates get() and source property changes
 *   through a lense.
 */
public  class LenseBinding
  implements Binding,PropertyChangeListener
{
    
  private final Binding _source;
  private final Lense _lense;
  private final Optic[] _modifiers;
  private final boolean _static;
  private PropertyChangeSupport _propertyChangeSupport;
  private PropertyChangeListener _modifierListener;

  public LenseBinding(Binding source,Lense lense,Optic[] modifiers)
  { 
    _source=source;
    _lense=lense;
    _modifiers=modifiers;

    // Determine default static by checking source, modifiers.
    // If all dependencies are static, this is static
    boolean isStatic=true;
    if (!_source.isStatic())
    { isStatic=false;
    }
    if (_modifiers!=null)
    { 
      for (int i=0;i<_modifiers.length;i++)
      { 
        if (!_modifiers[i].isStatic())
        { 
          isStatic=false;
          break;
        }
      }
    }
    _static=isStatic;

  }

  public final Object get()
  { return _lense.translateForGet(_source.get(),resolveModifiers());
  }

  protected final Object getSourceValue()
  { return _source.get();
  }

  protected final boolean isSourceStatic()
  { return _source.isStatic();
  }

  /**
   * Override if value can be set
   */
  public boolean set(Object value)
  { return false;
  }

  public final Class getTargetClass()
  { return _lense.getTargetClass();
  }

  /**
   * Override if standard definition of isStatic is false
   */
  public boolean isStatic()
  { return _static;
  }

  public void propertyChange(PropertyChangeEvent event)
  {
    if (_propertyChangeSupport!=null)
    {
      Object oldValue=null;
      Object newValue=null;

      Object[] modifiers=resolveModifiers();

      if (event.getOldValue()!=null)
      { oldValue=_lense.translateForGet(event.getOldValue(),modifiers);
      }
      if (event.getNewValue()!=null)
      { newValue=_lense.translateForGet(event.getNewValue(),modifiers);
      }

      if (oldValue!=newValue)
      { _propertyChangeSupport.firePropertyChange("",oldValue,newValue);
      }
    }
  }

  public void modifierChanged()
  {
    if (_propertyChangeSupport!=null)
    {
      Object newValue=null;

      Object[] modifiers=resolveModifiers();

      Object sourceValue=_source.get();
      if (sourceValue!=null)
      { newValue=_lense.translateForGet(sourceValue,modifiers);
      }
      _propertyChangeSupport.firePropertyChange("",null,newValue);
    }
  }

  public PropertyChangeSupport propertyChangeSupport()
  { 
    if (isStatic())
    { return null;
    }
    
    if (_propertyChangeSupport==null)
    { 
      _propertyChangeSupport=new PropertyChangeSupport(this);
      PropertyChangeSupport pcs=_source.propertyChangeSupport();
      if (pcs!=null)
      { pcs.addPropertyChangeListener(this);
      }
      
      _modifierListener
        =new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent event)
        { modifierChanged();
        }
      };

      if (_modifiers!=null)
      {
        for (int i=0;i<_modifiers.length;i++)
        { 
          pcs=_modifiers[i].propertyChangeSupport();
          if (pcs!=null)
          { pcs.addPropertyChangeListener(_modifierListener);
          }
        }
      }
    }
    return _propertyChangeSupport;
  }

  private final Object[] resolveModifiers()
  {
    if (_modifiers!=null)
    { 
      Object[] modifiers=new Object[_modifiers.length];
      for (int i=0;i<modifiers.length;i++)
      { modifiers[i]=_modifiers[i].get();
      }
      return modifiers;
    }
    return null;
  }

  protected final void firePropertyChange(String name,Object oldValue,Object newValue)
  {
    if (_propertyChangeSupport!=null)
    { _propertyChangeSupport.firePropertyChange(name,oldValue,newValue);
    }
  }

}
