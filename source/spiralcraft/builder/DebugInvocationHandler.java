package spiralcraft.builder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import spiralcraft.util.ArrayUtil;

public class DebugInvocationHandler
  implements InvocationHandler
{
  private final Class _interface;
  
  public DebugInvocationHandler(Class clazz)
  { _interface=clazz;
  }
  
  public Object invoke
    (Object proxy
    ,Method method,
    Object[] args
    )
    throws Throwable
  { 
    System.err.println
      (method.toString()
      +"("
      +ArrayUtil.formatToString(args,",","\"")
      +")"
      );
    return null;    
  }
}
