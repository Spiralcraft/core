package spiralcraft.tuple.lang;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Scheme;

import spiralcraft.tuple.spi.ArrayTuple;
import spiralcraft.tuple.spi.ReflectionScheme;

import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.AbstractBinding;
import spiralcraft.lang.optics.Binding;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;


/**
 * Implements the 'properties' portion of a Java interface via a Proxy,
 *   using a Tuple to delegate actual data storage.
 */
public class TupleDelegate
  extends DelegateBinding
  implements InvocationHandler
{
  
  private final Object _proxy;
  private final TupleBinding _binding;
  
  public TupleDelegate(Class iface)
    throws BindException
  { 
    super(iface);
    
    Scheme scheme=ReflectionScheme.getInstance(iface);
    _binding=new TupleBinding
      (scheme
      ,new ArrayTuple(scheme)
      );
      
    _proxy=Proxy.newProxyInstance
      (iface.getClassLoader()
      ,new Class[] {iface}
      ,this
      );
  }


  
  public Binding getBinding()
  { return _binding;
  }
 
  public Object invoke
    (Object proxy,
    Method method,
    Object[] args
    )
    throws Throwable
  {
    Field field=
      ((ReflectionScheme) _binding.getScheme()).getField(method);

    if (field!=null)
    {
      if (args!=null && args.length>0)
      {
        System.out.println(args[0]);
        // Write
        ((Tuple) _binding.get()).set(field,args[0]);
        return null;
      }
      else
      {
        // Read
        return ((Tuple) _binding.get()).get(field);
      }
    }
    else
    { 
      try
      { 
        // We should run this on a 'peer' of the Tuple
        //   instead of on the Tuple itself
        return method.invoke(_binding.get(),args);
      }
      catch (InvocationTargetException x)
      { throw x.getTargetException();
      }
    }
  }
  
  protected Object retrieve()
  { return _proxy;
  }
  
  public boolean store(Object val)
  { return false;
  }
}
