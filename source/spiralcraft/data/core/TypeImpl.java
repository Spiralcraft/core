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

import java.net.URI;

/**
 * Core implementation of a Type
 */
public class TypeImpl<T>
  extends Type<T>
{  
  protected Class<T> nativeClass;
  protected SchemeImpl scheme;
  protected final TypeResolver resolver;
  protected final URI uri;
  protected final URI packageURI;
  protected Type archetype;
  protected Type baseType;
  protected boolean aggregate=false;
  protected Type contentType=null;
  
  private boolean linked;
  
  public TypeImpl(TypeResolver resolver,URI uri)
  { 
    this.resolver=resolver;
    this.uri=uri;
    this.packageURI=resolver.getPackageURI(uri);
  }
  
  public URI getPackageURI()
  { return packageURI;
  }
  
  public Type getArchetype()
  { return archetype;
  }
  
  public void setArchetype(Type archetype)
  { 
    if (linked)
    { throw new IllegalStateException("Type already linked");
    }
    this.archetype=archetype;
  }
  
  public boolean hasArchetype(Type type)
  {
    if (this==type)
    { return true;
    }
    else if (archetype!=null)
    { return archetype.hasArchetype(type);
    }
    else
    { return false;
    }
  }
  
  public Type getBaseType()
  { return baseType;
  }
  
  public void setBaseType(Type baseType)
  { 
    if (linked)
    { throw new IllegalStateException("Type already linked");
    }
    this.baseType=baseType;
  }
  
  public boolean hasBaseType(Type type)
  {
    if (this==type)
    { return true;
    }
    else if (baseType!=null)
    { return baseType.hasBaseType(type);
    }
    else
    { return false;
    }
  }

  public boolean isAssignableFrom(Type type)
  {
    
    if (type.isPrimitive())
    { return getNativeClass().isAssignableFrom(type.getNativeClass());
    }
    else
    {
      // Check native compatability
      if (getNativeClass()!=null
          && type.getNativeClass()!=null
          && !getNativeClass().isAssignableFrom(type.getNativeClass())
          )
      { return false;
      }
    
      if (type.hasArchetype(this))
      { return true;
      }
      if (type.hasBaseType(this))
      { return true;
      }
      
      return false;
    }
  }
  
  public Type getMetaType()
  { 
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
  public Class<T> getNativeClass()
  { return nativeClass;
  }
  
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  
  public URI getURI()
  { return uri;
  }

  
  /**
   * Default implementation is to set up the SchemeImpl scheme with 
   *   arechetype and base type and resolve it.
   */
  public void link()
    throws DataException
  { 
    if (linked)
    { return;
    }
    linked=true;
    scheme.setType(this);
    if (archetype!=null && archetype.getScheme()!=null)
    { scheme.setArchetypeScheme(archetype.getScheme());
    }
    scheme.resolve();
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
    this.scheme=(SchemeImpl) scheme;
  }
  
  /**
   * Default implementation of validate is to ensure that the supplied object
   *   is assignable to this Type's nativeClass.
   */
  @SuppressWarnings("unchecked")
  public ValidationResult validate(Object o)
  { 
    if (nativeClass==null)
    { return null;
    }
    
    if (o!=null
        && !(nativeClass.isAssignableFrom(o.getClass()))
       )
    { 
      return new ValidationResult
        (o.getClass().getName()
        +" cannot be assigned to "
        +nativeClass.getName()
        );
    }
    else
    { return null;
    }    
  }
  
  /**
   * @return Whether this Type is an aggregate (array or collection) of another
   *   type.
   */
  public boolean isAggregate()
  { return aggregate;
  }
  
  public Type getCoreType()
  {
    Type ret=this;
    while (ret.isAggregate())
    { ret=ret.getContentType();
    }
    return ret;
  }

  public boolean isPrimitive()
  { return false;
  }
  
  public boolean isStringEncodable()
  { return false;
  }
  
  public boolean isDataEncodable()
  { return true;
  }
    
  public String toString()
  { return super.toString()+":"+(uri!=null?uri.toString():"(delegated)");
  }
  
  public String toString(T val)
  { throw new UnsupportedOperationException("Not string encodable");
  }
  
  public T fromString(String val)
    throws DataException
  { throw new UnsupportedOperationException("Not string encodable");
  }

  public T fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { throw new UnsupportedOperationException("Not depersistable");
  }
  
  public DataComposite toData(T val)
    throws DataException
  { throw new UnsupportedOperationException("Not persistable");
  }
  
  public Type getContentType()
  { return contentType;
  }
  
}
