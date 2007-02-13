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

import spiralcraft.data.core.ArrayType;

import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.StaticInstanceResolver;

import spiralcraft.data.wrapper.ReflectionType;

import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.PropertyBinding;
import spiralcraft.builder.BuildException;

import spiralcraft.stream.Resource;
import spiralcraft.stream.Resolver;
import spiralcraft.stream.UnresolvableURIException;

import spiralcraft.util.Path;

import java.net.URI;

import java.util.HashMap;
import java.util.List;

import java.io.IOException;


/**
 * A Type based on a spiralcraft.builder Assembly. The Scheme is defined
 *   by a combination of the AssemblyClass members and Bean reflection.
 */
public class BuilderType
  implements Type
{
  public static final char INNER_PATH_SEPARATOR='_';
  public static final URI GENERIC_BUILDER_TYPE_URI
    =URI.create("java:/spiralcraft/builder/Object");
    
  public static final URI GENERIC_BUILDER_ARRAY_TYPE_URI
    =URI.create("java:/spiralcraft/builder/Object.array");
  
  private final TypeResolver resolver;
  private final URI uri;
  private final AssemblyClass targetAssemblyClass;
  private final boolean aggregate=false;
  private final Class nativeClass=Assembly.class;
  private final HashMap<String,Type> pathMap
    =new HashMap<String,Type>();
  
  private boolean linked;
  private BuilderScheme scheme;
  private Type archetype;
  private Field classField;
  
  
  public static final boolean isApplicable(URI uri)
    throws DataException
  { 
    String uriString=uri.toString();
    int bangPos=uriString.indexOf(INNER_PATH_SEPARATOR);
    if (bangPos>=0)
    { uriString=uriString.substring(0,bangPos);
    }
    
    URI resourceUri=URI.create(uriString+".assembly.xml");
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

  public static URI canonicalUri(AssemblyClass assemblyClass)
  {
    if (assemblyClass!=null)
    {
      URI baseUri=
        TypeResolver.desuffix(assemblyClass.getSourceURI(),".assembly.xml");
      
      String path="";
      
      while (assemblyClass!=null && assemblyClass.getDefiningClass()!=null)
      { 
        if (path.length()>0)
        { path="/".concat(path);
        }
        if (assemblyClass.getId()!=null)
        { path=".".concat(assemblyClass.getId()).concat(path);
        }
        if (assemblyClass.getContainingProperty()==null)
        { 
          System.err.println("Null containing property "
            +assemblyClass.toString()+" defined in "
            +assemblyClass.getDefiningClass().toString());
        }
        path=assemblyClass.getContainingProperty().getTargetName().concat(path);
        assemblyClass=assemblyClass.getDefiningClass();
      }
      if (path.length()>0)
      { return URI.create(baseUri.toString()+INNER_PATH_SEPARATOR+path);
      }
      else
      { return baseUri;
      }
    }
    else
    { return null;
    }
  }
  
  public static Type canonicalType(AssemblyClass assemblyClass)
    throws DataException
  { return (TypeResolver.getTypeResolver().resolve(canonicalUri(assemblyClass)));
  }
  
  public static Type genericBuilderType()
    throws DataException
  { return TypeResolver.getTypeResolver().resolve(GENERIC_BUILDER_TYPE_URI);
  }
  
  public static Type genericBuilderArrayType()
    throws DataException
  { return TypeResolver.getTypeResolver().resolve(GENERIC_BUILDER_ARRAY_TYPE_URI);
  }
  
  
  public BuilderType
    (BuilderType outerType
    ,String pathElement
    )
    throws DataException
  { 
    this.resolver=outerType.getTypeResolver();
    
    String propertyName;
    String objectId;
    
    int dotPos=pathElement.indexOf(".");
    if (dotPos>=0)
    { 
      propertyName=pathElement.substring(0,dotPos);
      objectId=pathElement.substring(dotPos+1);
    }
    else
    {
      propertyName=pathElement;
      objectId=null;
    }
      
    AssemblyClass containingClass
      =outerType.getAssemblyClass();
    PropertySpecifier containingProperty
      =containingClass.getMember(propertyName);
    if (containingProperty==null)
    { 
      throw new DataException
        ("Creating inner type "+pathElement+" in "+outerType.toString()+": PropertySpecifier '"+propertyName+"' not found in AssemblyClass "
        +containingClass
        );
    }

    
    List<AssemblyClass> contents=containingProperty.getContents();
    if (objectId==null)
    { 
      if (contents.size()==1)
      { targetAssemblyClass=contents.get(0);
      }
      else
      {
        throw new DataException
          ("PropertySpecifier '"+propertyName+"' is ambiguous in AssemblyClass "
          +containingClass
          );
      }
    }
    else
    { 
      AssemblyClass target=null;
      for (AssemblyClass candidate: contents)
      {
        if (objectId.equals(candidate.getId()))
        { 
          target=candidate;
          break;
        }
      }
      if (target!=null)
      { targetAssemblyClass=target;
      }
      else
      {
        throw new DataException
          ("AssemblyClass with id '"+objectId+"'"
          +" in property '"+propertyName+"' "
          +"not found in AssemblyClass "
          +containingClass
          );
      }
      
      
    }
    this.uri=canonicalUri(targetAssemblyClass);
    System.err.println("Created Builder Type "+uri);
  }
  
  public BuilderType(TypeResolver resolver,URI uri)
    throws DataException
  {
    this.resolver=resolver;
    this.uri=uri;
    
    try
    {
      targetAssemblyClass
        =AssemblyLoader.getInstance()
          .findAssemblyDefinition(URI.create(uri.toString()+".assembly.xml"));
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }
    
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
  
  AssemblyClass getAssemblyClass()
  { return targetAssemblyClass;
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
    if (baseAssemblyClass!=null)
    { 
      archetype
        =resolver.resolve
          (canonicalUri(baseAssemblyClass)
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
          // XXX now we have better typing- make sure this is appropriate
          Class tupleClass=(Class) classField.getValue(t);
          for (Assembly assembly: contextBinding.getContents())
          { 
            System.out.println("Checking "+tupleClass+" = "+assembly.getSubject().get().getClass());
            if (assembly.getSubject().get().getClass().equals(tupleClass))
            { 
              System.out.println("Found");
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
    
    Assembly assembly=(Assembly) obj;
    
    if (assembly.getAssemblyClass()!=targetAssemblyClass)
    { 
      // System.out.println("Narrowing "+getUri());
      Type targetType
        =getTypeResolver().resolve
          (canonicalUri(assembly.getAssemblyClass())
          );
      return targetType.toData(assembly);    
    }
    else
    {
      // System.out.println("Not narrowing "+getUri());
      EditableTuple t=new EditableArrayTuple(scheme);
      scheme.persistBeanProperties( assembly ,t);
      return t;
    }
  }
  
  public String toString()
  { return super.toString()+":"+uri.toString();
  }
}
