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


import java.net.URI;



/**
 * A Type based on a spiralcraft.builder Assembly. The Scheme is defined
 *   by a combination of the AssemblyClass members and Bean reflection.
 */
@SuppressWarnings("unchecked") // Can't further specify Assembly generic
public class BuilderType
  extends TypeImpl<Assembly>
{
  public static final char INNER_PATH_SEPARATOR='_';
  public static final URI GENERIC_BUILDER_TYPE_URI
    =URI.create("java:/spiralcraft/builder/Object.assy");
    
  public static final URI GENERIC_BUILDER_ARRAY_TYPE_URI
    =URI.create("java:/spiralcraft/builder/Object.assy.array");
  
  private final AssemblyClass targetAssemblyClass;
  private boolean linked;

  //private final HashMap<String,Type> pathMap
  //  =new HashMap<String,Type>();
  
  private Field classField;
  
  { nativeClass=Assembly.class;
  }
  
  public static final boolean isApplicable(URI uri)
    throws DataException
  { return uri.getPath().endsWith(".assy");
  }

  @SuppressWarnings("unchecked") // Type system is heterogeneous
  public boolean isAssignableFrom(Type type)
  { 
    if (super.isAssignableFrom(type))
    { return true;
    }
    if (getURI().equals(GENERIC_BUILDER_TYPE_URI)
        && type instanceof BuilderType
        )
    { 
      // All builder types are compatible with the degenerate AssemblyClass
      return true;
    }
    System.out.println("BuilderType -isAssignableFrom "+this+" : "+type);
    return false;
  }
  
  public static URI canonicalURI(AssemblyClass assemblyClass)
  {
    if (assemblyClass!=null)
    {
      URI baseUri=
        TypeResolver.desuffix(assemblyClass.getSourceURI(),".assy.xml");
      
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
      { return URI.create(baseUri.toString()+INNER_PATH_SEPARATOR+path+".assy");
      }
      else
      { return URI.create(baseUri.toString()+".assy");
      }
    }
    else
    { return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public static Type<Assembly> canonicalType(AssemblyClass assemblyClass)
    throws DataException
  { return (Type<Assembly>) TypeResolver.getTypeResolver().<Assembly>resolve(canonicalURI(assemblyClass));
  }
  
  public static Type<?> genericBuilderType()
    throws DataException
  { return TypeResolver.getTypeResolver().resolve(GENERIC_BUILDER_TYPE_URI);
  }
  
  public static Type<?> genericBuilderArrayType()
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
      ,canonicalURI(innerClass)
      );

    targetAssemblyClass=innerClass;
      
//    System.err.println("Created Builder Type "+uri);
  }
  
  public BuilderType(TypeResolver resolver,URI uri)
    throws DataException
  {
    super(resolver,uri);
    
    try
    {
      URI assemblyURI=TypeResolver.desuffix(uri,".assy");
      targetAssemblyClass
        =AssemblyLoader.getInstance()
          .findAssemblyClass(assemblyURI);
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }
    
  }

  AssemblyClass getAssemblyClass()
  { return targetAssemblyClass;
  }

  public Class<?> getTargetType()
  { return targetAssemblyClass.getJavaClass();
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
      archetype=
        resolver.resolve
          (canonicalURI(baseAssemblyClass)
          );
    }
    BuilderScheme scheme=new BuilderScheme(resolver,this,targetAssemblyClass);
    scheme.addFields();
    this.scheme=scheme;
    super.link();
    classField=scheme.getFieldByName("class");

  }

  public Assembly<?> newAssembly(Assembly<?> parent)
    throws BuildException
  { return targetAssemblyClass.newInstance(parent);
  }
  
  /**
   * Translate a Tuple describing an Assembly object into
   *   either a new Assembly or an existing one in the
   *   the specified PropertyBinding context.
   */
  public Assembly<?> fromData(DataComposite composite,InstanceResolver resolver)
    throws DataException
  {
    
    PropertyBinding contextBinding=null;
    if (resolver!=null)
    { contextBinding=(PropertyBinding) resolver.resolve(PropertyBinding.class);
    }
    
    Tuple t = composite.asTuple();
    try
    { 
      Assembly<?> instance=null;
      if (contextBinding!=null)
      {
        if (contextBinding.isAggregate())
        { 
          // Disambiguate by Class
          // XXX now we have better typing- make sure this is appropriate
          Class<?> tupleClass=(Class<?>) classField.getValue(t);
          for (Assembly<?> assembly: contextBinding.getContents())
          { 
//            System.out.println("BuilderType: Checking "+tupleClass+" = "+assembly.getSubject().get().getClass());
            if (assembly.get().getClass().equals(tupleClass))
            { 
//              System.err.println("BuilderType: resolved existing target assembly for "+tupleClass);
              instance=assembly;
              break;
            }
          }
        }
        else
        { 
          Assembly<?>[] contents=contextBinding.getContents();
          if (contents.length>0)
          { 
//            System.err.println("BuilderType: resolved existing target assembly for "+getURI());
            instance=contents[0];
          }
        }
      }
      if (instance==null)
      { 
        // Construct a new Assembly as a child of the current context, if
        //   not null
        Assembly<?> parent=null;
        if (contextBinding!=null)
        { parent=contextBinding.getContainer();
        }
        instance=newAssembly(parent);
//        System.err.println("BuilderType: constructed new instance of "+getURI());
      }
      ((BuilderScheme) scheme).depersistBeanProperties(t,instance);
      return instance;
      
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }

  }
  
  @SuppressWarnings("unchecked") // Runtime downcast- can't use generic version
  public DataComposite toData(Assembly<?> obj)
    throws DataException
  {
    if (!(obj instanceof Assembly))
    { throw new DataException(obj.getClass().getName()+" is not an Assembly");
    }
    
    Assembly<?> assembly=(Assembly<?>) obj;
    
    if (assembly.getAssemblyClass()!=targetAssemblyClass)
    { 
      // System.out.println("Narrowing "+getUri());
      Type<Assembly> targetType
        =(BuilderType) getTypeResolver().<Assembly>resolve
          (canonicalURI(assembly.getAssemblyClass())
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
