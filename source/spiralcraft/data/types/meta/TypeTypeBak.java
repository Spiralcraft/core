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
package spiralcraft.data.types.meta;

import spiralcraft.data.Type;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.core.TypeImpl;

import spiralcraft.data.wrapper.ReflectionScheme;

import java.net.URI;

/**
 * A Type implementation that represents a Type
 */
public class TypeTypeBak
  extends TypeImpl
  implements Type
{
  private boolean linked;
  

  public TypeTypeBak(TypeResolver resolver,URI uri)
  { 
    super(resolver,uri);
    nativeType=Type.class;
  }
  
  public void link()
    throws DataException
  { 
    if (linked)
    { return;
    }
    linked=true;
    ReflectionScheme scheme=new ReflectionScheme(resolver,this,TypeImpl.class);
    scheme.resolve();
    this.scheme=scheme;
  }
  
  public ValidationResult validate(Object o)
  { return null;
  }
  
  public Object fromString(String val)
    throws DataException
  { return resolver.resolve(URI.create(val));
  }
  
  public String toString(Object val)
  { return ((Type) val).getUri().toString();
  } 

  public boolean isStringEncodable()
  { return true;
  }
  
  public boolean isPrimitive()
  { return false;
  }
  
  public DataComposite toData(Object val)
  { throw new UnsupportedOperationException("Not persistable");
  }
   
  
  public Object fromData(DataComposite data,InstanceResolver resolver)
  { throw new UnsupportedOperationException("Not depersistable");
  }
}