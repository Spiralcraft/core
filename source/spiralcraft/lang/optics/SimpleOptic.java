package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

/**
 * An abstract Optic for implementing base types
 */
public class SimpleOptic
  implements Optic
{
 
  private Class _class;
  private Object _object;

  /**
   * Create a SimpleOptic with the specified Object as its target
   *   and with a targetClass equals to the Object's class.
   */
  public SimpleOptic(Object val)
  { 
    _object=val;
    _class=_object.getClass();
  }

  public SimpleOptic(Class clazz,Object val)
  { 
    _object=val;
    _class=clazz;
  }

  public Optic resolve(Focus focus,String name,Expression[] params)
  { return null;
  }

  public Object get()
  { return _object;
  }

  public boolean set(Object value)
  { 
    _object=value;
    return true;
  }

  public Class getTargetClass()
  { return _class;
  }

}
