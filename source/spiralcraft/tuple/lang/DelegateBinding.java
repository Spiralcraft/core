package spiralcraft.tuple.lang;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;

import spiralcraft.lang.optics.AbstractBinding;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Binding;

public abstract class DelegateBinding
  extends AbstractBinding
{
  public DelegateBinding(Class clazz)
    throws BindException
  { super(new DelegatePrism(clazz),true);
  }
  
  public abstract Binding getBinding();

}

class DelegatePrism
  implements Prism
{
  private final Class _class;
  
  public DelegatePrism(Class clazz)
  { _class=clazz;
  }
  
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Binding binding=((DelegateBinding) source).getBinding();
    return binding.getPrism().resolve(binding,focus,name,params);
  }
  
  public Class getJavaClass()
  { return _class;
  }
}
