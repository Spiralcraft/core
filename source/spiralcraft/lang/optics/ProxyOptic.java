package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import java.beans.PropertyChangeSupport;

/**
 * An Optic which delegates to another Optic, usually in order to
 *   decorate the namespace.
 */
public class ProxyOptic
  implements Optic
{

  private final Optic _optic;

  public ProxyOptic(Optic delegate)
  { 
    if (delegate==null)
    { throw new IllegalArgumentException("Delegate cannot be null");
    }
    _optic=delegate;
  }

  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  { return _optic.resolve(focus,name,params);
  }

  public Object get()
  { return _optic.get();
  }

  public boolean set(Object value)
  { return _optic.set(value);
  }

  public Class getContentType()
  { return _optic.getContentType();
  }

  public Decorator decorate(Class decoratorInterface)
  { return _optic.decorate(decoratorInterface);
  }

  public PropertyChangeSupport propertyChangeSupport()
  { return _optic.propertyChangeSupport();
  }

  public boolean isStatic()
  { return _optic.isStatic();
  }

  public Prism getPrism()
  { return _optic.getPrism();
  }
  
  public String toString()
  { return super.toString()+":"+_optic.toString();
  }
}
