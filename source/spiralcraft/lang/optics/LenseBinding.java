package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

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
  private static int _ID=0;
  
  protected final String id;

  private final Optic _source;
  private final Lense _lense;
  private final Optic[] _modifiers;
  private final boolean _static;
  private PropertyChangeSupport _propertyChangeSupport;
  private PropertyChangeListener _modifierListener;
  private WeakBindingCache _cache;

  /**
   * Create a Lense binding which translates values bidirectionally,
   *   through the specified lense.
   */
  public LenseBinding(Optic source,Lense lense,Optic[] modifiers)
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
    id=getClass().getName()+":"+_ID++;  

  }

  public synchronized WeakBindingCache getCache()
  {
    if (_cache==null)
    { _cache=new WeakBindingCache();
    }
    return _cache;
  }
  
  public final Object get()
  { return _lense.translateForGet(_source.get(),resolveModifiers());
  }

  /**
   * Indicates that downstream bindings have requested property change
   *   notifications
   */
  protected boolean isPropertyChangeSupportActive()
  { return _propertyChangeSupport!=null;
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

  public final Prism getPrism()
  { return _lense.getPrism();
  }

  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Binding binding=_lense.getPrism().resolve(this,focus,name,params);
    if (binding==null)
    { throw new BindException("'"+name+"' not found in "+_lense.getPrism().toString());
    }
    return binding;
  }
  
  public Class getContentType()
  { return _lense.getPrism().getContentType();
  }
  
  public Decorator decorate(Class decoratorInterface)
  { 
    try
    { return _lense.getPrism().decorate(this,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error decorating",x);
    }
  }
  
  /**
   * Override if standard definition of isStatic is false
   */
  public boolean isStatic()
  { return _static;
  }

  public void propertyChange(PropertyChangeEvent event)
  {
    // System.out.println(toString()+".propertyChange");
    if (_propertyChangeSupport!=null)
    {
      // System.out.println(toString()+".propertyChange2");
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
      { 
        // System.out.println(toString()+".propertyChange3");
        _propertyChangeSupport.firePropertyChange("",oldValue,newValue);
      }
    }
  }

  public void modifierChanged()
  {
    // System.out.println(toString()+".modifierChanged");
    if (_propertyChangeSupport!=null)
    {
      Object newValue=null;

      Object[] modifiers=resolveModifiers();

      Object sourceValue=_source.get();
      if (sourceValue!=null)
      { newValue=_lense.translateForGet(sourceValue,modifiers);
      }
      // System.out.println(toString()+".modifierChanged:"+newValue);
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
      // System.out.println(toString()+" propertyChangeSupport");
      
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
          { 
            // System.out.println(toString()+" added modifier listener");
            pcs.addPropertyChangeListener(_modifierListener);
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
    // System.out.println(toString()+".firePropertyChange: "+newValue);
    if (_propertyChangeSupport!=null)
    { 
      // System.out.println(toString()+".firePropertyChange2: "+newValue);
      _propertyChangeSupport.firePropertyChange(name,oldValue,newValue);
    }
  }

  protected final void firePropertyChange(PropertyChangeEvent event)
  {
    if (_propertyChangeSupport!=null)
    { _propertyChangeSupport.firePropertyChange(event);
    }
  }

  public String toString()
  { return getClass().getName()+":"+id+"["+_lense.toString()+"]";
  }
}
