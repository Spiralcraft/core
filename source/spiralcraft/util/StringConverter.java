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
package spiralcraft.util;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.net.URI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Converts a String to an object of a specific type. Provides a means for registering
 *   StringConverters for custom types.
 */
public abstract class StringConverter<T>
{

  private static final HashMap<Class,StringConverter<?>> _MAP
    =new HashMap<Class,StringConverter<?>>();
    
  private static final StringConverter _ONE_WAY_INSTANCE=new ToString();
  
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
    _MAP.put(BigInteger.class,new BigIntegerToString());
    _MAP.put(BigDecimal.class,new BigDecimalToString());
    _MAP.put(Class.class,new ClassToString());
    _MAP.put(URI.class,new URIToString());
  }

  /**
   * Get an appropriate instance of the StringConverter for the specified type
   */
  public static StringConverter<?> getInstance(Class<?> type)
  { 
    synchronized (_MAP)
    { 
      StringConverter<?> ret=_MAP.get(type);
      if (ret==null)
      { 
        // Discover single argument constructor and create a new
        //   stringConverter for the class

        Constructor constructor=null;
        try
        { constructor=type.getConstructor(new Class[] {String.class});
        }
        catch (Exception x)
        { }

        if (constructor!=null)
        { ret=new ConstructFromString(constructor);
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
  public static void registerInstance(Class type,StringConverter converter)
  {
    synchronized (_MAP)
    { _MAP.put(type,converter);
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
  public String toString(T val)
  { return val!=null?val.toString():null;
  }

  /**
   * Convert a String to an Object
   */
  public abstract T fromString(String val);

  
}

final class ConstructFromString
  extends StringConverter<Object>
{
  private Constructor _constructor;

  public ConstructFromString(Constructor constructor)
  { _constructor=constructor;
  }
  
  public Object fromString(String val)
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
  { return val!=null?Integer.parseInt(val):null;
  }
}

final class BooleanToString
  extends StringConverter
{
  public String toString(Object val)
  { return val!=null?val.toString():null;
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
  { return val!=null?Float.parseFloat(val):null;
  }
}

final class LongToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?Long.parseLong(val):null;
  }
}

final class DoubleToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?Double.parseDouble(val):null;
  }
}

final class ShortToString
  extends StringConverter
{
  public Object fromString(String val)
  { return val!=null?Short.parseShort(val):null;
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
  { return val!=null?Byte.parseByte(val):null;
  }
}

final class BigDecimalToString
  extends StringConverter<BigDecimal>
{
  public BigDecimal fromString(String val)
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
    { throw new IllegalArgumentException("Class "+val+" not found.");
    }

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
