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
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.DataComposite;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.wrapper.ReflectionType;

import java.net.URI;

/**
 * Core implementation of a Type
 */
public class TypeImpl
  implements Type
{  
  protected Class nativeType;
  protected Scheme scheme;
  protected final TypeResolver resolver;
  protected final URI uri;
  protected boolean linked;
  
  public TypeImpl(TypeResolver resolver,URI uri)
  { 
    this.resolver=resolver;
    this.uri=uri;
  }
  
  
  public Type getMetaType()
  { 
    
    // XXX When do we want to get a meta type
    //   from the .type tag operator-
    //   for the type extender
    //  
    // .type
    //    fromData() method should instantiate Type interface
    // 
    // prototype definitions (.type.xml)
    //    just data, until resolved through TypeResolver
    //    Won't instantiate on it's own
    // 
    try
    { return resolver.resolve(URI.create(uri.toString().concat(".type")));
    }
    catch (TypeNotFoundException x)
    { throw new RuntimeException(x);
    }
  }
  
  /**
   * The public Java class or interface used to programatically access or
   *   manipulate this data element.
   */
  public Class getNativeClass()
  { return nativeType;
  }
  
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  
  public URI getUri()
  { return uri;
  }

  public void link()
    throws DataException
  { 
    if (linked)
    { return;
    }
    linked=true;
  }
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  public Scheme getScheme()
  { return scheme;
  }
  
  public void setScheme(Scheme scheme)
  {
    if (linked)
    { throw new IllegalStateException("Type already linked");
    }
    // System.out.println("SETTING SCHEME");
    this.scheme=scheme;
    ((SchemeImpl) scheme).setType(this);
    ((SchemeImpl) scheme).resolve();
  }
  
  public ValidationResult validate(Object o)
  { return null;
  }
  
  /**
   * @return Whether this Type is an aggregate (array or collection) of another
   *   type.
   */
  public boolean isAggregate()
  { return false;
  }
  
  public Type getCoreType()
  { return this;
  }

  public boolean isPrimitive()
  { return false;
  }
  
  public boolean isStringEncodable()
  { return false;
  }
  
  public String toString()
  { return super.toString()+":"+(uri!=null?uri.toString():"(delegated)");
  }
  
  public String toString(Object val)
  { throw new UnsupportedOperationException("Not string encodable");
  }
  
  public Object fromString(String val)
    throws DataException
  { throw new UnsupportedOperationException("Not string encodable");
  }

  public Object fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { throw new UnsupportedOperationException("Not depersistable");
  }
  
  public DataComposite toData(Object val)
    throws DataException
  { throw new UnsupportedOperationException("Not persistable");
  }
  
  public Type getContentType()
  { return null;
  }
  
}
