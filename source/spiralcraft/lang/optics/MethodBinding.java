package spiralcraft.lang.optics;


import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class MethodBinding
  extends LenseBinding
{
  public MethodBinding
    (Binding source
    ,MethodLense lense
    )
  { super(source,lense,null);
  }

  public boolean isStatic()
  { return false;
  }

}


