package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;


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

  public MethodBinding
    (Binding source
    ,MethodLense lense
    ,Optic[] params
    )
  { super(source,lense,params);
  }

  public boolean isStatic()
  { return false;
  }

}


