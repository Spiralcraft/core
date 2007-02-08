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
package spiralcraft.data.meta;

import spiralcraft.data.Type;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Scheme;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.ConstructorInstanceResolver;

import spiralcraft.data.sax.DataReader;

import spiralcraft.data.wrapper.ReflectionType;

import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertyBinding;
import spiralcraft.builder.BuildException;

import spiralcraft.stream.Resource;
import spiralcraft.stream.Resolver;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * A Type based on Tuple data, which defines the Type, Scheme and Fields in
 *   a type.xml data file.
 */
public class ProtoType
  implements Type
{
  private static final URI TYPE_TYPE_URI
    =URI.create("java:/spiralcraft/data/types/meta/Type");
  
  private final TypeResolver resolver;
  private final URI uri;
  private boolean linked;

  private Type delegate;
  
  
  public static final boolean isApplicable(URI uri)
    throws DataException
  { 
    URI resourceUri=URI.create(uri.toString()+".type.xml");
    Resource resource=null;
    
    try
    { resource=Resolver.getInstance().resolve(resourceUri);
    }
    catch (UnresolvableURIException x)
    { return false;
    } 
    
    try
    { return resource.exists();
    }
    catch (IOException x)
    { 
      throw new DataException
        ("IOException checking resource "+resourceUri+": "+x.toString()
        ,x
        );
    }
  }

  public ProtoType(TypeResolver resolver,URI uri)
    throws DataException
  {
    this.resolver=resolver;
    this.uri=uri;
  }

  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;
    try
    {
      Tuple tuple
        =(Tuple) new DataReader()
          .readFromUri(URI.create(uri.toString()+".type.xml")
                      ,resolver.resolve(TYPE_TYPE_URI)
                      );

      InstanceResolver instanceResolver
        =new ConstructorInstanceResolver
          (new Class[] {TypeResolver.class,URI.class}
          ,new Object[] {resolver,uri}
          );
          
          
      delegate=(Type) tuple.getType().fromData(tuple,instanceResolver);
      delegate.link(); 
    }
    catch (SAXException x)
    { throw new DataException(x.toString(),x);
    }
    catch (IOException x)
    { throw new DataException(x.toString(),x);
    }
    
  }
  
  public URI getUri()
  { return uri;
  }
  
  public TypeResolver getTypeResolver()
  { return resolver;
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
   
  public Class getNativeClass()
  { return delegate.getNativeClass();
  }
  
  public Type getContentType()
  { return delegate.getContentType();
  }
  
  public Type getCoreType()
  { return delegate.getCoreType();
  }

  public boolean isAggregate()
  { return delegate.isAggregate();
  }

  public boolean isPrimitive()
  { return delegate.isPrimitive();
  }
  
  public Scheme getScheme()
  { return delegate.getScheme();
  }

  public ValidationResult validate(Object val)
  { return delegate.validate(val);
  }  
  

  public boolean isStringEncodable()
  { return delegate.isStringEncodable();
  }
  
  public Object fromString(String str)
    throws DataException
  { return delegate.fromString(str);
  }

  public String toString(Object val)
  { return delegate.toString(val);
  }
  
  public Object fromData(DataComposite composite,InstanceResolver resolver)
    throws DataException
  { return delegate.fromData(composite,resolver);
  }
  
  public DataComposite toData(Object obj)
    throws DataException
  { return delegate.toData(obj);
  }
  
  public String toString()
  { return super.toString()+":"+uri.toString()+" is-a "+delegate.toString();
  }
}
