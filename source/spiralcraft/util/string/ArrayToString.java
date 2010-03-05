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

/**
 * <p>Translates an array of objects to a comma-delimited list and back. 
 * </p>
 * 
 * @author mike
 *
 * @param <Tdata>
 */
public final class ArrayToString<Tdata>
  extends StringConverter<Tdata[]>
{
  private final StringConverter<Tdata> converter;
  private final Class<Tdata> componentClass;
  
  @SuppressWarnings("unchecked")
  public ArrayToString(Class<Tdata> componentClass)
  { 
    this.converter
      =(StringConverter<Tdata>) StringConverter.getInstance(componentClass);
    this.componentClass=componentClass;
  }
  
  public ArrayToString
    (StringConverter<Tdata> converter
    ,Class<Tdata> componentClass
    )
  {
    this.converter=converter;
    this.componentClass=componentClass;
  }
  
  @Override
  public String toString(Tdata[] val)
  { 
    if (val==null)
    { return null;
    }
    
    StringBuilder buf=new StringBuilder();
    for (Tdata item : val)
    { 
      if (buf.length()>0)
      { buf.append(",");
      }
      buf.append(converter.toString(item));
    }
    return buf.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  /**
   * <p>Turns a comma delimited list into an array of the target type.
   * </p>
   * 
   * <p>XXX: Add escaping code commas in strings ie. \, and \\
   * </p>
   */
  public Tdata[] fromString(String val)
  { 
    
    String[] strings=StringUtil.tokenize(val,",");
    Tdata[] data=(Tdata[]) Array.newInstance(componentClass,strings.length);
    for (int i=0;i<strings.length;i++)
    { data[i]=converter.fromString(strings[i]);
    }
    return data;
  }
}