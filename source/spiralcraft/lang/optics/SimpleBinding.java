package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;

import java.beans.PropertyChangeSupport;

/**
 * An Binding bound to a self-contained Object 
 */
public class SimpleBinding
  extends AbstractBinding
{
 
  private Object _object;
  
  /**
   * Create a SimpleOptic with the specified Object as its target
   *   and with a targetClass equals to the Object's class.
   */
  public SimpleBinding(Object val,boolean isStatic)
    throws BindException
  { 
    super(OpticFactory.getInstance().findPrism(val.getClass()),isStatic);
    _object=val;
  }

  public SimpleBinding(Class clazz,Object val,boolean isStatic)
    throws BindException
  { 
    super(OpticFactory.getInstance().findPrism(clazz),isStatic);
    _object=val;
  }

  
  protected Object retrieve()
  { return _object;
  }
  
  protected boolean store(Object val)
  { 
    _object=val;
    return true;
  }

}
