package spiralcraft.util;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.net.URI;

/**
 * Converts a String to an object of a specific type. Provides a means for registering
 *   StringConverters for custom types.
 */
public abstract class StringConverter
{

  private static final HashMap _MAP=new HashMap();
  private static final StringConverter _ONE_WAY_INSTANCE=new ToString();
  
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
    _MAP.put(Character.class.getName(),new CharacterToString());
    _MAP.put(Character.TYPE.getName(),new CharacterToString());
    _MAP.put(BigInteger.class.getName(),new BigIntegerToString());
    _MAP.put(BigDecimal.class.getName(),new BigDecimalToString());
    _MAP.put(Class.class.getName(),new ClassToString());
    _MAP.put(URI.class.getName(),new URIToString());
  }

  /**
   * Get an appropriate instance of the StringConverter for the specified type
   */
  public static StringConverter getInstance(Class type)
  { 
    synchronized (_MAP)
    { 
      StringConverter ret=(StringConverter) _MAP.get(type.getName());
      if (ret==null)
      { 
        // Discover single argument constructor and create a new
        //   stringConverter for the class
      }
      return ret;
    }
    
  }

  public static StringConverter getOneWayInstance()
  { return _ONE_WAY_INSTANCE;
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

  public static Object decodeFromXml(String xml)
  {
    XMLDecoder decoder
      =new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
    return decoder.readObject();
    
  }

  public static String encodeToXml(Object object)
  {
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    XMLEncoder encoder=new XMLEncoder(out);
    encoder.writeObject(object);
    encoder.close();
    return out.toString();
  }

  /**
   * Convert an Object to a String
   */
  public String toString(Object val)
  { return val!=null?val.toString():null;
  }

  /**
   * Convert a String to an Object
   */
  public abstract Object fromString(String val);

  
}

final class ToString
  extends StringConverter
{
  
  public Object fromString(String val)
  { throw new UnsupportedOperationException();
  }
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
  public Object fromString(String val)
  { return val!=null?new Float(val):null;
  }
}

final class LongToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?new Long(val):null;
  }
}

final class DoubleToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?new Double(val):null;
  }
}

final class ShortToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?new Short(val):null;
  }
}

final class CharacterToString
  extends StringConverter
{
  public Object fromString(String val)
  { return (val!=null && val.length()==1)?new Character(val.charAt(0)):null;
  }
}

final class ByteToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?new Byte(val):null;
  }
}

final class BigDecimalToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?new BigDecimal(val):null;
  }
}

final class BigIntegerToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?new BigInteger(val):null;
  }
}

final class ClassToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((Class) val).getName():null;
  }

  /**
   * Convert a ClassName to a String by loading the class.
   *
   * Packageless classes are resolved to the java.lang package
   *   first, then the 'null' package.
   *
   * Names of primitive types and primitive array types 
   *  resolve to their primitive class.
   */
  public Object fromString(String val)
  { 
    val=val.intern();    
    if (!val.contains("."))
    {
      if (val=="boolean")
      { return boolean.class;
      }
      if (val=="boolean[]")
      { return boolean[].class;
      }
      if (val=="byte")
      { return byte.class;
      }
      if (val=="byte[]")
      { return byte[].class;
      }
      if (val=="short")
      { return short.class;
      }
      if (val=="short[]")
      { return short[].class;
      }
      if (val=="char")
      { return char.class;
      }
      if (val=="char[]")
      { return char[].class;
      }
      if (val=="int")
      { return int.class;
      }
      if (val=="int[]")
      { return int[].class;
      }
      if (val=="long")
      { return long.class;
      }
      if (val=="long[]")
      { return long[].class;
      }
      if (val=="double")
      { return double.class;
      }
      if (val=="double[]")
      { return double[].class;
      }
      if (val=="float")
      { return float.class;
      }
      if (val=="float[]")
      { return float[].class;
      }
      if (val=="String")
      { return String.class;
      }
      if (val=="String[]")
      { return String[].class;
      }
      if (val=="Class")
      { return Class.class;
      }
      if (val=="Class[]")
      { return Class[].class;
      }
      
      try
      { return Class.forName("java.lang."+val);
      }
      catch (ClassNotFoundException x)
      { }
      
    }
      
    try
    {
      
      return Class.forName
        (val
        ,false
        ,Thread.currentThread().getContextClassLoader()
        );
    }
    catch (ClassNotFoundException x)
    { x.printStackTrace();
    }
    return null;
  }
}

final class URIToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?((URI) val).toString():null;
  }

  public Object fromString(String val)
  { return URI.create(val);
  }
}
