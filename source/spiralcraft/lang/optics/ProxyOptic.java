package spiralcraft.lang.optics;

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

  public Optic resolve(String name,Expression[] params)
  { return _optic.resolve(name,params);
  }

  public Object get()
  { return _optic.get();
  }

  public boolean set(Object value)
  { return _optic.set(value);
  }
}
