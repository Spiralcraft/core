package spiralcraft.util;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Converts a String to an object of a specific type. Provides a means for registering
 *   StringConverters for custom types.
 */
public abstract class StringConverter
{

  private static final HashMap _MAP=new HashMap();
  
  static
  {
    _MAP.put(String.class.getName(),new StringToString());
    _MAP.put(Integer.class.getName(),new IntToString());
    _MAP.put(Integer.TYPE.getName(),new IntToString());
    _MAP.put(Boolean.class.getName(),new BooleanToString());
    _MAP.put(Boolean.TYPE.getName(),new BooleanToString());
    _MAP.put(Float.class.getName(),new FloatToString());
    _MAP.put(Float.TYPE.getName(),new FloatToString());
    _MAP.put(Long.class.getName(),new LongToString());
    _MAP.put(Long.TYPE.getName(),new LongToString());
    _MAP.put(Double.class.getName(),new DoubleToString());
    _MAP.put(Double.TYPE.getName(),new DoubleToString());
    _MAP.put(Short.class.getName(),new ShortToString());
    _MAP.put(Short.TYPE.getName(),new ShortToString());
    _MAP.put(Byte.class.getName(),new ByteToString());
    _MAP.put(Byte.TYPE.getName(),new ByteToString());
    _MAP.put(BigInteger.class.getName(),new BigIntegerToString());
    _MAP.put(BigDecimal.class.getName(),new BigDecimalToString());
  }

  /**
   * Get an appropriate instance of the StringConverter for the specified type
   */
  public static StringConverter getInstance(Class type)
  { 
    synchronized (_MAP)
    { return (StringConverter) _MAP.get(type.getName());
    }
    
  }

  /**
   * Register an instance of a StringConverter to be used for the specified type
   */
  public static void registerInstance(Class type,StringConverter converter)
  {
    synchronized (_MAP)
    { _MAP.put(type.getName(),converter);
    }
  }

  /**
   * Convert an Object to a String
   */
  public abstract String toString(Object val);

  /**
   * Convert a String to an Object
   */
  public abstract Object fromString(String val);

  
}

final class StringToString
  extends StringConverter
{
  public String toString(Object val)
  { return (String) val;
  }

  public Object fromString(String val)
  { return val;
  }
}

final class IntToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Integer) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new Integer(val):null;
  }
}

final class BooleanToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Boolean) val).booleanValue()?"true":"false":null;
  }

  public Object fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    else if (val.equals("true"))
    { return Boolean.TRUE;
    }
    else if (val.equals("false"))
    { return Boolean.FALSE;
    }
    else
    { throw new IllegalArgumentException(val);
    }
  }
}

final class FloatToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Float) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new Float(val):null;
  }
}

final class LongToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Long) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new Long(val):null;
  }
}

final class DoubleToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Double) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new Double(val):null;
  }
}

final class ShortToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Short) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new Short(val):null;
  }
}

final class ByteToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Byte) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new Byte(val):null;
  }
}

final class BigDecimalToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((BigDecimal) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new BigDecimal(val):null;
  }
}

final class BigIntegerToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((BigInteger) val).toString():null;
  }

  public Object fromString(String val)
  { return val!=null?new BigInteger(val):null;
  }
}

