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
package spiralcraft.data.builder;

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
import spiralcraft.data.spi.StaticInstanceResolver;

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


/**
 * A Type based on a spiralcraft.builder Assembly. The Scheme is defined
 *   by a combination of the AssemblyClass members and Bean reflection.
 */
public class BuilderType
  implements Type
{
  private final TypeResolver resolver;
  private final URI uri;
  private final AssemblyClass baseAssemblyClass;
  private final AssemblyClass targetAssemblyClass;
  private final boolean aggregate=false;
  private final Class nativeClass;
  
  private boolean linked;
  private BuilderScheme scheme;
  private Type archetype;
  private Field classField;
  
  
  public static final boolean isApplicable(URI uri)
    throws DataException
  { 
    URI resourceUri=URI.create(uri.toString()+".assembly.xml");
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

  public BuilderType(TypeResolver resolver,URI uri)
    throws DataException
  {
    this.resolver=resolver;
    this.uri=uri;
    
    try
    {
      // XXX Adapt to using Fragment to reference inner definitions
      baseAssemblyClass
        =AssemblyLoader.getInstance()
          .findAssemblyDefinition(URI.create(uri.toString()+".assembly.xml"));
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }
    
    targetAssemblyClass=baseAssemblyClass;
    nativeClass=Assembly.class;
  }

  public URI getUri()
  { return uri;
  }
  
  public TypeResolver getTypeResolver()
  { return resolver;
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
  
  public Class getNativeClass()
  { return nativeClass;
  }
  
  public Type getContentType()
  { return null;
  }
  
  public Type getCoreType()
  { return this;
  }
  
  public Class getTargetType()
  { return targetAssemblyClass.getJavaClass();
  }
  

  public boolean isAggregate()
  { return aggregate;
  }

  public boolean isPrimitive()
  { return false;
  }
  
  public Scheme getScheme()
  { return scheme;
  }

  public ValidationResult validate(Object val)
  { 
    if (val!=null
        && !(nativeClass.isAssignableFrom(val.getClass()))
       )
    { 
      return new ValidationResult
        (val.getClass().getName()
        +" cannot be assigned to "
        +nativeClass.getName()
        );
    }
    else
    { return null;
    }
  }  
  
  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;
    
    AssemblyClass baseAssemblyClass=targetAssemblyClass.getBaseClass();
    if (baseAssemblyClass!=null && baseAssemblyClass.getDefiningClass()==null)
    { 
      archetype
        =resolver.resolve
          (resolver.desuffix
            (baseAssemblyClass.getSourceURI()
            ,".assembly.xml"
            )
          );
    }
    
    scheme=new BuilderScheme(resolver,this,targetAssemblyClass);
    if (archetype!=null && archetype.getScheme()!=null)
    { scheme.setArchetypeScheme(archetype.getScheme());
    }    
    scheme.resolve();
    classField=scheme.getFieldByName("class");

  }

  public boolean isStringEncodable()
  { return false;
  }
  
  public Object fromString(String str)
  {
    throw new UnsupportedOperationException
      ("Type has no String representation");
  }

  public String toString(Object val)
  {
    throw new UnsupportedOperationException
      ("Type has no String representation");
  }
  
  public Assembly newAssembly(Assembly parent)
    throws BuildException
  { return targetAssemblyClass.newInstance(parent);
  }
  
  /**
   * Translate a Tuple describing an Assembly object into
   *   either a new Assembly or an existing one in the
   *   the specified PropertyBinding context.
   */
  public Object fromData(DataComposite composite,InstanceResolver resolver)
    throws DataException
  {
    
    PropertyBinding contextBinding=null;
    if (resolver!=null)
    { contextBinding=(PropertyBinding) resolver.resolve(PropertyBinding.class);
    }
    
    Tuple t = composite.asTuple();
    try
    { 
      Assembly instance=null;
      if (contextBinding!=null)
      {
        if (contextBinding.isAggregate())
        { 
          // Disambiguate by Class
          Class tupleClass=(Class) classField.getValue(t);
          for (Assembly assembly: contextBinding.getContents())
          { 
            if (assembly.getSubject().get().getClass().equals(tupleClass))
            { 
              instance=assembly;
              break;
            }
          }
        }
        else
        { 
          Assembly[] contents=contextBinding.getContents();
          if (contents.length>0)
          { instance=contents[0];
          }
        }
      }
      if (instance==null)
      { 
        // Construct a new Assembly as a child of the current context, if
        //   not null
        Assembly parent=null;
        if (contextBinding!=null)
        { parent=contextBinding.getContainer();
        }
        instance=newAssembly(parent);
      }
      scheme.depersistBeanProperties(t,instance);
      return instance;
      
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }

  }
  
  public DataComposite toData(Object obj)
    throws DataException
  {
    if (!(obj instanceof Assembly))
    { throw new DataException(obj.getClass().getName()+" is not an Assembly");
    }
    EditableTuple t=new EditableArrayTuple(scheme);
    scheme.persistBeanProperties( (Assembly) obj ,t);
    return t;
  }
  
  public String toString()
  { return super.toString()+":"+uri.toString();
  }
}
