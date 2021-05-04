package spiralcraft.util.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import spiralcraft.common.Coercion;

public abstract class NumericCoercion<T>
  implements Coercion<Number,T>
{

  private static final HashMap<Class<?>,NumericCoercion<?>> map
    =new HashMap<Class<?>,NumericCoercion<?>>();
  
  public static final NumericCoercion<?> instance(Class<?> target)
  {
    if (target.isPrimitive())
    { target=ClassUtil.boxedEquivalent(target);
    }
    NumericCoercion<?> coercion=map.get(target);
    if (coercion==null)
    { coercion=makeCoercion(target);
    }
    if (coercion!=null)
    { map.put(target,coercion);
    }
    return coercion;
  }
  
  private static final NumericCoercion<?> makeCoercion(Class<?> target)
  {
    NumericCoercion<?> coercion=null;
    if (target.equals(Float.class))
    {
      coercion=
        new NumericCoercion<Float>() 
      {
        @Override
        public Float coerce(Number val)
        { return val==null?null:val.floatValue();
        }
      };
    }
    else if (target.equals(Long.class))
    {
      coercion= 
          new NumericCoercion<Long>() 
      {
        @Override
        public Long coerce(Number val)
        { return val==null?null:val.longValue();
        }
      };
    }
    else if (target.equals(Double.class))
    {
      coercion= 
          new NumericCoercion<Double>() 
      {
        @Override
        public Double coerce(Number val)
        { return val==null?null:val.doubleValue();
        }
      };
    }     
    else if (target.equals(Integer.class))
    {
      coercion= 
          new NumericCoercion<Integer>() 
      {
        @Override
        public Integer coerce(Number val)
        { return val==null?null:val.intValue();
        }
      };
    }
    else if (target.equals(Short.class))
    {
      coercion= 
          new NumericCoercion<Short>() 
      {
        @Override
        public Short coerce(Number val)
        { return val==null?null:val.shortValue();
        }
      };
    }     
    else if (target.equals(Byte.class))
    {
      coercion= 
          new NumericCoercion<Byte>() 
      {
        @Override
        public Byte coerce(Number val)
        { return val==null?null:val.byteValue();
        }
      };
    }              
    else if (target.equals(BigDecimal.class))
    {
      coercion= 
          new NumericCoercion<BigDecimal>() 
      {
        @Override
        public BigDecimal coerce(Number val)
        { return val==null?null:BigDecimal.valueOf(val.doubleValue());
        }
      };
    }              
    else if (target.equals(BigInteger.class))
    {
      coercion=
          new NumericCoercion<BigInteger>() 
      {
        @Override
        public BigInteger coerce(Number val)
        { return val==null?null:BigInteger.valueOf(val.intValue());
        }
      };
    }  
    
    return coercion;
  }
}
