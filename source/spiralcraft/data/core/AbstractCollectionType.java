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
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.wrapper.ReflectionType;

import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.util.ArrayUtil;


import java.lang.reflect.Array;

import java.net.URI;
import java.util.Collection;

/**
 * Base type for Collections
 */
public class AbstractCollectionType
  implements Type
{  
  private final TypeResolver resolver;
  private final Type contentType;
  private final URI uri;
  private final Class nativeType;
  
  public AbstractCollectionType
    (TypeResolver resolver
    ,Type contentType
    ,URI uri
    ,Class<? extends Collection> nativeType
    )
  { 
    this.resolver=resolver;
    if (contentType==null)
    { 
      try
      {
        this.contentType=getTypeResolver().resolve
          (ReflectionType.canonicalUri(Object.class));
      }
      catch (TypeNotFoundException x)
      { throw new RuntimeException(x);
      }
    }
    else
    { this.contentType=contentType;
    }
    this.uri=uri;
    this.nativeType=nativeType;
  }

  public Type getMetaType()
  {
    try
    { return getTypeResolver().resolve(ReflectionType.canonicalUri(getClass()));
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
  
  /**
   * Arrays are always represented by aggregates
   */
  public boolean isPrimitive()
  { return false;
  }
  
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  
  public URI getUri()
  { return uri;
  }
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  public Scheme getScheme()
  { return contentType.getScheme();
  }
  
  public boolean isAggregate()
  { return true;
  }
  
  public Type getContentType()
  { return contentType;
  }
  
  public Type getCoreType()
  {
    Type ret=this;
    while (ret.isAggregate())
    { ret=ret.getContentType();
    }
    return ret;
  }
  
  public ValidationResult validate(Object value)
  { 
    // More work here-
    //   Go through values and and call type.validate() on each one
    // Validation result should accept multiple messages
    return null;
  }
  
  public void link()
    throws DataException
  { contentType.link();
  }
  
  public boolean isStringEncodable()
  { return true;
  }
  
  public Object fromString(String val)
  { return null;
  }
  
  public String toString(Object val)
  { return null;
  }
  
  public Object fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { 
    Aggregate aggregate=data.asAggregate();
    
    Collection collection=null;
    
    if (resolver!=null)
    { collection=(Collection) resolver.resolve(nativeType);
    }
    
    if (collection==null)
    { 
      try
      { collection=(Collection) nativeType.newInstance();
      }
      catch (InstantiationException x)
      { 
        throw new DataException
          ("Error instantiating collection "
          +nativeType.getName()+": "+x.toString()
          ,x
          );
      }
      catch (IllegalAccessException x)
      { 
        throw new DataException
          ("Error instantiating collection "
          +nativeType.getName()+": "+x.toString()
          ,x
          );
      }
      
    }
    
    for (Object val: aggregate)
    { 
//      if (contentType.isPrimitive())
      if (!(val instanceof DataComposite))
      { collection.add(val);
      }
      else
      { 
        Type valueType=((DataComposite) val).getType();
        Object convertedVal=valueType.fromData((DataComposite) val,resolver);
        collection.add(convertedVal);
      }
    }
    return collection;
  }
  
  public DataComposite toData(Object obj)
    throws DataException
  { 
    if (!(obj instanceof Collection))
    { throw new IllegalArgumentException("Not a collection");
    }
    
    Collection collection=(Collection) obj;
    
    EditableAggregate aggregate=new EditableArrayListAggregate(this);

    for (Object o: collection)
    {
      if (contentType.isPrimitive())
      { aggregate.add(o);
      }
      else
      { aggregate.add(contentType.toData(o));
      }
    }
    
    return aggregate;
  }
  
    
  public String toString()
  { return super.toString()+":"+uri.toString();
  }
}
