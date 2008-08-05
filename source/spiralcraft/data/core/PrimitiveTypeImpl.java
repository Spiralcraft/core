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
import spiralcraft.data.DataException;
import spiralcraft.data.util.InstanceResolver;

import spiralcraft.util.StringConverter;

import java.net.URI;

public abstract class PrimitiveTypeImpl<T>
  extends TypeImpl<T>
{
  protected boolean linked;
  protected StringConverter<T> converter;
  protected boolean dataEncodable=false;
    
  @SuppressWarnings("unchecked")
  public PrimitiveTypeImpl(TypeResolver resolver,URI uri,Class<T> nativeClass)
  { 
    super(resolver,uri);
    this.nativeClass=nativeClass;
    
    converter=(StringConverter<T>) StringConverter.getInstance(nativeClass);
  }
  
  protected void setStringConverter(StringConverter<T> converter)
  { this.converter=converter;    
  }
  
  @Override
  public boolean isPrimitive()
  { return true;
  }
  
  @Override
  public final synchronized void link()
    throws DataException
  { 
    if (!linked)
    { 
      linkPrimitive();
      super.link();
      linked=true;
    }
  }
  
  
  
  @Override
  public String toString(T val)
  { 
    if (val==null)
    { return null;
    }
    if (nativeClass.isAssignableFrom(val.getClass()))
    { 
      if (converter!=null)
      { return converter.toString(val);
      }
      else
      { return StringConverter.encodeToXml(val);
      }
    }
    throw new IllegalArgumentException(val.getClass()+" is not an String");

  }
  
  @Override
  public T fromString(String string)
    throws DataException
  { 
    if (converter!=null)
    { return converter.fromString(string);
    }
    else
    { return StringConverter.<T>decodeFromXml(string);
    }
  }
  
  @Override
  public DataComposite toData(T val)
  { 
    throw new UnsupportedOperationException
      ("Type "+uri+" is primitive");
  }
  
  @Override
  public T fromData(DataComposite t,InstanceResolver resolver)
  {
    throw new UnsupportedOperationException
      ("Type "+uri+" is primitive");
  }
  
  /**
   * Primitive types should always be String encodable
   */
  @Override
  public boolean isStringEncodable()
  { return true;
  }
  
  @Override
  public boolean isDataEncodable()
  { return dataEncodable;
  }
  
  /**
   * Primitive types should override to perform recursive linking behavior
   */
  protected void linkPrimitive()
  {
  }

  
}