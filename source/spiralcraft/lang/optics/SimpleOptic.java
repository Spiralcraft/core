package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;

import java.beans.PropertyChangeSupport;

/**
 * An Optic which uses a Prism to provide a namespace
 *   for a Binding.
 */
public class SimpleOptic
  implements Optic
{
 
  private final Binding _binding;
  private final Prism _prism;
  private final OpticFactory _factory;

  public SimpleOptic(Binding binding)
    throws BindException
  { this(binding,OpticFactory.getInstance());
  }

  public SimpleOptic(Binding binding,OpticFactory factory)
    throws BindException
  { this(binding,factory.findPrism(binding),factory);
  }

  /**
   * Create a SimpleOptic with the specified Binding and Prism
   */
  public SimpleOptic(Binding binding,Prism prism)
  { 
    if (binding==null)
    { throw new IllegalArgumentException("Binding cannot be null");
    }
    _binding=binding;
    _prism=prism;
    _factory=OpticFactory.getInstance();
  }

  /**
   * Create a SimpleOptic with the specified Binding and Prism
   *   and a user supplied factory
   */
  public SimpleOptic(Binding binding,Prism prism,OpticFactory factory)
  { 
    _binding=binding;
    _prism=prism;
    if (factory==null)
    { _factory=OpticFactory.getInstance();
    }
    else
    { _factory=factory;
    }

  }

  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  { 
    if (_prism!=null)
    { 
      Binding binding=_prism.resolve(_binding,focus,name,params);
      if (binding==null)
      { throw new BindException("'"+name+"' not found in "+_prism.toString());
      }
      return new SimpleOptic(binding,_factory);
    }
    else
    { return null;
    }
  }

  public Object get()
  { return _binding.get();
  }

  public boolean set(Object value)
  { return _binding.set(value);
  }

  public Class getTargetClass()
  { return _binding.getTargetClass();
  }

  public boolean isStatic()
  { return _binding.isStatic();
  }

  public PropertyChangeSupport propertyChangeSupport()
  { return _binding.propertyChangeSupport();
  }
}
