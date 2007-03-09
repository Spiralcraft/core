//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.core;

import spiralcraft.data.DataComposite;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.InstanceResolver;

import spiralcraft.util.StringConverter;

import java.net.URI;

public abstract class PrimitiveTypeImpl<T>
  extends TypeImpl<T>
{
  protected boolean linked;
  protected StringConverter<T> converter;
    
  @SuppressWarnings("unchecked")
  public PrimitiveTypeImpl(TypeResolver resolver,URI uri,Class<T> nativeClass)
  { 
    super(resolver,uri);
    this.nativeClass=nativeClass;
    this.converter=(StringConverter<T>) StringConverter.getInstance(nativeClass);
  }
  
  protected void setStringConverter(StringConverter<T> converter)
  { this.converter=converter;    
  }
  
  public boolean isPrimitive()
  { return true;
  }
  
  public final synchronized void link()
  { 
    if (!linked)
    { 
      linkPrimitive();
      linked=true;
    }
  }
  
  
  
  @SuppressWarnings("unchecked")
  public String toString(T val)
  {
    if (val==null)
    { return null;
    }
    if (nativeClass.isAssignableFrom(val.getClass()))
    { return converter.toString(val);
    }
    throw new IllegalArgumentException(val.getClass()+" is not an String");

  }
  
  public T fromString(String string)
  { return (T) converter.fromString(string);
  }
  
  public DataComposite toData(T val)
  { 
    throw new UnsupportedOperationException
      ("Type "+uri+" is primitive");
  }
  
  public T fromData(DataComposite t,InstanceResolver resolver)
  {
    throw new UnsupportedOperationException
      ("Type "+uri+" is primitive");
  }
  
  /**
   * Primitive types should always be String encodable
   */
  public boolean isStringEncodable()
  { return converter!=null;
  }
  
  /**
   * Primitive types should override to perform recursive linking behavior
   */
  protected void linkPrimitive()
  {
  }

  
}