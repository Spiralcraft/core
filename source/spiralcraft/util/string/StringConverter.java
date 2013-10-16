//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.util.string;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.net.URI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.common.Coercion;
import spiralcraft.util.refpool.URIPool;

/**
 * <p>Converts bidirectionally between a String and an object of a specific
 *    type in a consistent manner.
 * </p>
 * 
 * <p>Provides a static interface for registering standard StringConverters 
 *   for custom types.
 * </p>
 */
public abstract class StringConverter<T>
  implements Coercion<String,T>
{

  // TODO: Make weak so classes can be collected
  private static final HashMap<Class<?>,StringConverter<?>> _MAP
    =new HashMap<Class<?>,StringConverter<?>>();
    
  private static final StringConverter<?> _ONE_WAY_INSTANCE=new ToString();
  
  static
  {
    
    _MAP.put(String.class,new StringToString());
    _MAP.put(Integer.class,new IntToString());
    _MAP.put(int.class,new IntToString());
    _MAP.put(Boolean.class,new BooleanToString());
    _MAP.put(boolean.class,new BooleanToString());
    _MAP.put(Float.class,new FloatToString());
    _MAP.put(float.class,new FloatToString());
    _MAP.put(Long.class,new LongToString());
    _MAP.put(long.class,new LongToString());
    _MAP.put(Double.class,new DoubleToString());
    _MAP.put(double.class,new DoubleToString());
    _MAP.put(Short.class,new ShortToString());
    _MAP.put(short.class,new ShortToString());
    _MAP.put(Byte.class,new ByteToString());
    _MAP.put(byte.class,new ByteToString());
    _MAP.put(Character.class,new CharacterToString());
    _MAP.put(char.class,new CharacterToString());
    _MAP.put(char[].class,new CharacterArrayToString());
    _MAP.put(BigInteger.class,new BigIntegerToString());
    _MAP.put(BigDecimal.class,new BigDecimalToString());
    _MAP.put(Class.class,new ClassToString());
    _MAP.put(URI.class,new URIToString());
    _MAP.put(Pattern.class,new PatternToString());
    _MAP.put(byte[].class,new ByteArrayToHex());
    
    _MAP.put(spiralcraft.time.Instant.class,new InstantToString());
    _MAP.put(spiralcraft.time.Duration.class,new DurationToString());    
  }

  /**
   * <p>Get an appropriate instance of the StringConverter for the specified 
   *   type.
   * </p>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Type logic
  public static <T> StringConverter<T> getInstance(Class type)
  { 
    
    synchronized (_MAP)
    { 
      StringConverter<T> ret=(StringConverter<T>) _MAP.get(type);
      if (ret==null)
      { 
        // Discover single argument constructor and create a new
        //   stringConverter for the class
        if (type.isArray())
        { 
          StringConverter componentInstance
            =getInstance(type.getComponentType());
          if (componentInstance!=null)
          { ret=new ArrayToString(componentInstance,type.getComponentType());
          }
        }
        else if (type.isEnum())
        { ret=new EnumToString(type);
        }
        else
        {
          Constructor<T> constructor=null;
          try
          { constructor=type.getConstructor(new Class[] {String.class});
          }
          catch (Exception x)
          { }

          if (constructor!=null)
          { ret=new ConstructFromString<T>(constructor);
          }
        }
        
        if (ret!=null)
        { _MAP.put(type,ret);
        }
      }
      return ret;
    }
    
  }
  
  public static StringConverter<?> getOneWayInstance()
  { return _ONE_WAY_INSTANCE;
  }

  /**
   * Register an instance of a StringConverter to be used for the specified type
   */
  public static void registerInstance
    (Class<?> type,StringConverter<?> converter)
  {
    synchronized (_MAP)
    { 
      if (_MAP.get(type)!=null)
      { 
        throw new IllegalArgumentException
          (type+" is already registered to use "+_MAP.get(type)+" as a default"
           +" StringConverter and cannot be mapped to "+converter
           );
      }
      _MAP.put(type,converter);
    }
  }

  @SuppressWarnings("unchecked") // XMLDecoder is not generic
  public static <T> T decodeFromXml(String xml)
  {
    XMLDecoder decoder
      =new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
    return (T) decoder.readObject();
    
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
   * Convert an array of Objects to an array of Strings using the 
   *   provided array of StringConverters.
   *   
   * @param converters
   * @param input
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static String[] toStrings(StringConverter[] converters,Object[] input)
  { 
    String[] result=new String[input.length];
    for (int i=0;i<input.length;i++)
    { result[i]=converters[i].toString(input[i]);
    }
    return result;
  }
  
  
  /**
   * Convert an array of Strings to an array of Objects using the 
   *   provided array of StringConverters.
   *   
   * @param converters
   * @param input
   * @return
   */
  public static Object[] fromStrings
    (StringConverter<?>[] converters,String[] input)
  { 
    Object[] result=new Object[input.length];
    for (int i=0;i<input.length;i++)
    { result[i]=converters[i].fromString(input[i]);
    }
    return result;
  }

  /**
   * Convert an Object to a String
   */
  public String toString(T val)
  { return val!=null?val.toString():null;
  }

  /**
   * Convert a String to an Object
   */
  public abstract T fromString(String val);

  /**
   * Implement the Coercion interface by calling fromString(input)
   */
  @Override
  public final T coerce(String input)
  { return fromString(input);
  }
  
}

final class ConstructFromString<T>
  extends StringConverter<T>
{
  private Constructor<T> _constructor;

  public ConstructFromString(Constructor<T> constructor)
  { _constructor=constructor;
  }
  
  @Override
  public T fromString(String val)
  { 
    try
    { return _constructor.newInstance(new Object[] {val});
    }
    catch (InvocationTargetException x)
    { 
      throw new IllegalArgumentException
        ("Error constructing object"
        ,x.getTargetException()
        );
    }
    catch (Exception x)
    {
      throw new IllegalArgumentException
        ("Error constructing object"
        ,x
        );
    }
  }
}

final class ToString
  extends StringConverter<Object>
{
  
  @Override
  public Object fromString(String val)
  { throw new UnsupportedOperationException();
  }
}

final class StringToString
  extends StringConverter<String>
{
  @Override
  public String toString(String val)
  { return val;
  }

  @Override
  public String fromString(String val)
  { return val;
  }
}





final class IntToString
  extends StringConverter<Integer>
{
  @Override
  public Integer fromString(String val)
  { return (val!=null && !val.isEmpty())?Integer.parseInt(val):null;
  }
}

final class BooleanToString
  extends StringConverter<Boolean>
{
  @Override
  public String toString(Boolean val)
  { return val!=null?val.toString():null;
  }

  @Override
  public Boolean fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    val=val.trim();
    if (val.equals("true"))
    { return Boolean.TRUE;
    }
    else if (val.equals("false"))
    { return Boolean.FALSE;
    }
    else
    { throw new IllegalArgumentException("["+val+"]");
    }
  }
}

final class FloatToString
  extends StringConverter<Float>
{
  @Override
  public Float fromString(String val)
  { return (val!=null && !val.isEmpty())?Float.parseFloat(val):null;
  }
}

final class LongToString
  extends StringConverter<Long>
{
  @Override
  public Long fromString(String val)
  { return (val!=null && !val.isEmpty())?Long.parseLong(val):null;
  }
}

final class DoubleToString
  extends StringConverter<Double>
{
  @Override
  public Double fromString(String val)
  { return (val!=null && !val.isEmpty())?Double.parseDouble(val):null;
  }
}

final class ShortToString
  extends StringConverter<Short>
{
  @Override
  public Short fromString(String val)
  { return (val!=null && !val.isEmpty())?Short.parseShort(val):null;
  }
}

final class CharacterToString
  extends StringConverter<Character>
{
  @Override
  public Character fromString(String val)
  { return (val!=null && val.length()==1)?Character.valueOf(val.charAt(0)):null;
  }
}

final class CharacterArrayToString
  extends StringConverter<char[]>
{
  @Override
  public char[] fromString(String val)
  { return (val!=null?val.toCharArray():null);
  }
  
  @Override
  public String toString(char[] val)
  { return String.copyValueOf(val);
  }
}



final class ByteToString
  extends StringConverter<Byte>
{
  @Override
  public Byte fromString(String val)
  { return (val!=null && !val.isEmpty())?Byte.parseByte(val):null;
  }
}


final class BigDecimalToString
  extends StringConverter<BigDecimal>
{
  static BigDecimal[] common
    ={BigDecimal.ZERO
     ,BigDecimal.ONE
     ,BigDecimal.valueOf(2)
     ,BigDecimal.valueOf(3)
     ,BigDecimal.valueOf(4)
     ,BigDecimal.valueOf(5)
     ,BigDecimal.valueOf(6)
     ,BigDecimal.valueOf(7)
     ,BigDecimal.valueOf(8)
     ,BigDecimal.valueOf(9)
     ,BigDecimal.TEN
     };
      
  
  @Override
  public BigDecimal fromString(String val)
  { 
    if (val==null || val.isEmpty())
    { return null;
    }
    
    if (val.length()<4)
    { 
      double dval=Double.parseDouble(val);
      if (dval % 1 == 0)
      { 
        int i=(int) dval;
        if (i>=0 && i<=10)
        { return common[i];
        }
      }
      return BigDecimal.valueOf(dval);
    }
    else
    { 
      BigDecimal ret=new BigDecimal(val);
      if (ret.compareTo(BigDecimal.ZERO)==0)
      { return BigDecimal.ZERO;
      }
      if (ret.equals(BigDecimal.ONE))
      { return BigDecimal.ONE;
      }
      return ret;
    }
  }
}

final class BigIntegerToString
  extends StringConverter<BigInteger>
{
  static BigInteger[] common
    ={BigInteger.ZERO
     ,BigInteger.ONE
     ,BigInteger.valueOf(2)
     ,BigInteger.valueOf(3)
     ,BigInteger.valueOf(4)
     ,BigInteger.valueOf(5)
     ,BigInteger.valueOf(6)
     ,BigInteger.valueOf(7)
     ,BigInteger.valueOf(8)
     ,BigInteger.valueOf(9)
     ,BigInteger.TEN
     };

  @Override
  public BigInteger fromString(String val)
  { 
    if (val==null || val.isEmpty())
    { return null;
    }

    if (val.length()<4)
    { 
      long lval=Long.parseLong(val);
      if (lval>=0 && lval<=10)
      { return common[(int) lval];
      }
      return BigInteger.valueOf(lval);
    }
    else
    {
      BigInteger ret=new BigInteger(val);
      if (ret.equals(BigInteger.ONE))
      { return BigInteger.ONE;
      }
      else if (ret.equals(BigInteger.ZERO))
      { return BigInteger.ZERO;
      }
      return ret;
    }
    
  }
}

final class ClassToString
  extends StringConverter<Class<?>>
{
  @Override
  public String toString(Class<?> val)
  { return val!=null?val.getName():null;
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
  @Override
  public Class<?> fromString(String val)
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
    { throw new IllegalArgumentException("Class "+val+" not found.");
    }

  }
}

final class URIToString
  extends StringConverter<URI>
{
  @Override
  public String toString(URI val)
  { return val!=null?(val).toString():null;
  }

  @Override
  public URI fromString(String val)
  { return URIPool.create(val);
  }
}

final class PatternToString
  extends StringConverter<Pattern>
{
  @Override
  public String toString(Pattern val)
  { return val!=null?(val).toString():null;
  }

  @Override
  public Pattern fromString(String val)
  { return Pattern.compile(val);
  }
}
