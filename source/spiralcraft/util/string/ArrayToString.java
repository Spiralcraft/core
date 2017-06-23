//
// Copyright (c) 2009,2010 Michael Toth
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

import java.lang.reflect.Array;

import spiralcraft.util.thread.CycleDetector;

/**
 * <p>Translates a homogenous array of objects to a delimited list and back. 
 * </p>
 * 
 * @author mike
 *
 * @param <Tdata>
 */
public final class ArrayToString<Tdata>
  extends StringConverter<Tdata[]>
{
  private static final CycleDetector<Class<?>> cycleDetector
    =new CycleDetector<Class<?>>();
  
  private final StringConverter<Tdata> converter;
  private final Class<Tdata> componentClass;
  private final char escapeChar;
  private final char delimiter;
  private final int capacity;
  
  /**
   * Converts an Array to a comma-delimited String using the default converter 
   *   for the specified component class. Commas and backslashes in the text 
   *   will be escaped with backslashes.
   *   
   * @param componentClass
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ArrayToString(Class<Tdata> componentClass)
  { 
    if (componentClass.isArray())
    { 
      if (cycleDetector.detectOrPush(componentClass))
      { 
        // Terminate an infinite loop
        this.converter
          =(StringConverter<Tdata>) StringConverter.getOneWayInstance();
      }
      else
      { 
        try
        { this.converter=new ArrayToString(componentClass.getComponentType());
        }
        finally
        { cycleDetector.pop();
        }
      }
    }
    else
    {
      StringConverter converter
        =(StringConverter<Tdata>) StringConverter.getInstance(componentClass);
      this.converter
        =converter!=null?converter:(StringConverter<Tdata>) StringConverter.getOneWayInstance();
    }
    
    this.componentClass=componentClass;
    this.escapeChar='\\';
    this.delimiter=',';
    this.capacity=10;
  }
  
  /**
   * <p>Converts an Array to a comma-delimited String using the specified 
   *   converter. Commas and backslashes in the text 
   *   will be escaped with backslashes. 
   * </p>
   * 
   * <p>This is the default constructor
   *   used for automatic conversion.
   * </p>
   * 
   * @param converter
   * @param componentClass
   */
  public ArrayToString
    (StringConverter<Tdata> converter
    ,Class<Tdata> componentClass
    )
  {
    this.converter=converter;
    this.componentClass=componentClass;
    this.escapeChar='\\';
    this.delimiter=',';
    this.capacity=10;
  }
  
  /**
   * <p>Converts an Array to a delimited String using the specified
   *   converter, delimiter, escape character, and initial buffer capacity
   * </p>
   * 
   * @param converter
   * @param componentClass
   * @param delimiter
   * @param escapeChar
   * @param capacity
   */
  public ArrayToString
    (StringConverter<Tdata> converter
    ,Class<Tdata> componentClass
    ,char delimiter
    ,char escapeChar
    ,int capacity
    )
  {
    this.converter=converter;
    this.componentClass=componentClass;
    this.escapeChar=escapeChar;
    this.delimiter=delimiter;
    this.capacity=capacity;
  }
  
  
  @Override
  public String toString(Tdata[] val)
  { 
    if (val==null)
    { return null;
    }
    
    String[] input=new String[val.length];
    for (int i=0;i<input.length;i++)
    { input[i]=converter.toString(val[i]);
    }
    return StringUtil.implode(delimiter,escapeChar,input);
  }

  @Override
  /**
   * <p>Turns a delimited list into an array of the target type.
   * </p>
   * 
   */
  @SuppressWarnings("unchecked")
  public Tdata[] fromString(String val)
  { 
    
    String[] strings=StringUtil.explode(val,delimiter,escapeChar,capacity);
    Tdata[] data=(Tdata[]) Array.newInstance(componentClass,strings.length);
    for (int i=0;i<strings.length;i++)
    { data[i]=converter.fromString(strings[i]);
    }
    return data;
  }
}