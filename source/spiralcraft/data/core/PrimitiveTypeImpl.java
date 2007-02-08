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

import spiralcraft.data.Type;
import spiralcraft.data.DataComposite;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.wrapper.ReflectionType;

import spiralcraft.util.StringConverter;

import java.net.URI;

public abstract class PrimitiveTypeImpl
  extends TypeImpl
  implements Type
{
  protected boolean linked;
  protected final StringConverter converter;
    
  public PrimitiveTypeImpl(TypeResolver resolver,URI uri,Class nativeType)
  { 
    super(resolver,uri);
    this.nativeType=nativeType;
    this.converter=StringConverter.getInstance(nativeType);
    if (this.converter==null)
    { System.err.println("No StringConverter for "+nativeType);
    }
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
  
  
  public ValidationResult validate(Object val)
  {
    if (val==null)
    { return null;
    }
    if (nativeType.isAssignableFrom(val.getClass()))
    { return null;
    }
    return new ValidationResult
      (val.getClass()+" cannot be assigned to "+nativeType);
  }
  
  public String toString(Object val)
  {
    if (val==null)
    { return null;
    }
    if (nativeType.isAssignableFrom(val.getClass()))
    { return converter.toString(val);
    }
    throw new IllegalArgumentException(val.getClass()+" is not an String");

  }
  
  public Object fromString(String string)
  { return converter.fromString(string);
  }
  
  public DataComposite toData(Object val)
  { 
    throw new UnsupportedOperationException
      ("Type "+uri+" is primitive");
  }
  
  public Object fromData(DataComposite t,InstanceResolver resolver)
  {
    throw new UnsupportedOperationException
      ("Type "+uri+" is primitive");
  }
  
  /**
   * Primitive types should always be String encodable
   */
  public boolean isStringEncodable()
  { return true;
  }
  
  /**
   * Primitive types should override to perform recursive linking behavior
   */
  protected void linkPrimitive()
  {
  }

  
}