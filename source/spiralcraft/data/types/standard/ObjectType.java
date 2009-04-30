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
package spiralcraft.data.types.standard;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;
import spiralcraft.data.reflect.ReflectionType;

import java.net.URI;

public class ObjectType
  extends PrimitiveTypeImpl<Object>
{
  private Type<Void> voidType;
  
  public ObjectType(TypeResolver resolver,URI uri)
  { 
    super(resolver,uri,Object.class);
  }
  
  
  @Override
  public boolean isAssignableFrom(Type<?> type)
  { 
    if (voidType!=null)
    { 
      try
      { voidType=ReflectionType.canonicalType(Void.class);
      }
      catch (DataException x)
      { throw new RuntimeException("Couldn't load Void type",x);
      }
    }
    return type!=voidType;
  }

  
  @Override
  public boolean isPrimitive()
  { return false;
  }
       
}