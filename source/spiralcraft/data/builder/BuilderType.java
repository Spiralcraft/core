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
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataException;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.core.TypeImpl;

import spiralcraft.data.spi.EditableArrayTuple;

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
  extends TypeImpl<Assembly>
  implements Type<Assembly>
{
  public static final char INNER_PATH_SEPARATOR='_';
  public static final URI GENERIC_BUILDER_TYPE_URI
    =URI.create("java:/spiralcraft/builder/Object");
    
  public static final URI GENERIC_BUILDER_ARRAY_TYPE_URI
    =URI.create("java:/spiralcraft/builder/Object.array");
  
  private final AssemblyClass targetAssemblyClass;

  //private final HashMap<String,Type> pathMap
  //  =new HashMap<String,Type>();
  
  private Field classField;
  
  { nativeClass=Assembly.class;
  }
  
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
  
  @SuppressWarnings("unchecked")
  public static Type<Assembly> canonicalType(AssemblyClass assemblyClass)
    throws DataException
  { return (Type<Assembly>) TypeResolver.getTypeResolver().resolve(canonicalUri(assemblyClass));
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
    ,AssemblyClass innerClass
    )
    throws DataException
  { 
    super
      (outerType.getTypeResolver()
      ,canonicalUri(innerClass)
      );

    targetAssemblyClass=innerClass;
      
    System.err.println("Created Builder Type "+uri);
  }
  
  public BuilderType(TypeResolver resolver,URI uri)
    throws DataException
  {
    super(resolver,uri);
    
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

  AssemblyClass getAssemblyClass()
  { return targetAssemblyClass;
  }

  public Class getTargetType()
  { return targetAssemblyClass.getJavaClass();
  }
  

  
  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    
    AssemblyClass baseAssemblyClass=targetAssemblyClass.getBaseClass();
    if (baseAssemblyClass!=null)
    { 
      archetype=
        resolver.resolve
          (canonicalUri(baseAssemblyClass)
          );
    }
    scheme=new BuilderScheme(resolver,this,targetAssemblyClass);
    super.link();
    classField=scheme.getFieldByName("class");

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
  public Assembly fromData(DataComposite composite,InstanceResolver resolver)
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
      ((BuilderScheme) scheme).depersistBeanProperties(t,instance);
      return instance;
      
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }

  }
  
  public DataComposite toData(Assembly obj)
    throws DataException
  {
    if (!(obj instanceof Assembly))
    { throw new DataException(obj.getClass().getName()+" is not an Assembly");
    }
    
    Assembly assembly=(Assembly) obj;
    
    if (assembly.getAssemblyClass()!=targetAssemblyClass)
    { 
      // System.out.println("Narrowing "+getUri());
      Type<Assembly> targetType
        =(BuilderType) getTypeResolver().resolve
          (canonicalUri(assembly.getAssemblyClass())
          );
      return targetType.toData(assembly);    
    }
    else
    {
      // System.out.println("Not narrowing "+getUri());
      EditableTuple t=new EditableArrayTuple(scheme);
      ((BuilderScheme) scheme).persistBeanProperties( assembly ,t);
      return t;
    }
  }
  
}
