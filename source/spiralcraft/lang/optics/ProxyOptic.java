package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

/**
 * An Optic which delegates to another Optic, usually in order to
 *   decorate the namespace.
 */
public class ProxyOptic
  implements Optic
{

  private final Optic _optic;

  public ProxyOptic(Optic delegate)
  { _optic=delegate;
  }

  public Optic resolve(Focus focus,String name,Expression[] params)
  { return _optic.resolve(focus,name,params);
  }

  public Object get()
  { return _optic.get();
  }

  public boolean set(Object value)
  { return _optic.set(value);
  }

  public Class getTargetClass()
  { return _optic.getTargetClass();
  }
}
