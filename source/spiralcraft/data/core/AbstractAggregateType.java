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

import java.net.URI;

/**
 * Implementation base class for common aggregate type functionality
 */
public abstract class AbstractAggregateType
  implements Type
{
  protected final URI uri;
  
  protected Type contentType;
  protected Class nativeClass;
  protected Type archetype;
  protected boolean linked;
  
  protected AbstractAggregateType(URI uri)
  { this.uri=uri;
  }
  
  public Type getArchetype()
  { return archetype;
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
  { return nativeClass;
  }

  /**
   * Aggregate Types are always represented by Aggregates in data
   */
  public boolean isPrimitive()
  { return false;
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
  
  public void link()
    throws DataException
  { 
    if (linked)
    { return;
    }
    linked=true;
    contentType.link();
    
    Type contentArchetype=contentType.getArchetype();
    if (contentArchetype!=null)
    { 
      archetype
        =contentType.getTypeResolver().resolve
          (URI.create(contentArchetype.getUri().toString().concat(".array"))
          );
    }
  }

  public boolean isStringEncodable()
  { return false;
  }
  
  public Object fromString(String val)
  { return null;
  }
  
  public String toString(Object val)
  { return null;
  }  

  public String toString()
  { return super.toString()+":"+uri.toString();
  }  
}