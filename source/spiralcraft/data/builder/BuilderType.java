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

import spiralcraft.data.core.TypeImpl;

import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.util.InstanceResolver;
import spiralcraft.lang.Focus;
import spiralcraft.util.refpool.URIPool;


import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertySpecifier;

import spiralcraft.builder.PropertyBinding;
import spiralcraft.builder.BuildException;


import java.net.URI;



/**
 * A Type based on a spiralcraft.builder Assembly. The Scheme is defined
 *   by a combination of the AssemblyClass members and Bean reflection.
 */
@SuppressWarnings({ "unchecked", "cast" ,"rawtypes" }) // Can't further specify Assembly generic
public class BuilderType
  extends TypeImpl<Assembly<?>>
{
  public static final char INNER_PATH_SEPARATOR='_';
  public static final URI GENERIC_BUILDER_TYPE_URI
    =URIPool.create("class:/spiralcraft/builder/Object.assy");
    
  public static final URI GENERIC_BUILDER_ARRAY_TYPE_URI
    =URIPool.create("class:/spiralcraft/builder/Object.assy.array");
  
  private final AssemblyClass targetAssemblyClass;
  private boolean linked;

  //private final HashMap<String,Type> pathMap
  //  =new HashMap<String,Type>();
  
  private Field classField;
  
  { nativeClass=(Class<Assembly<?>>) (Class) Assembly.class;
  }
  
  public static final boolean isApplicable(URI uri)
  { return uri.getPath().endsWith(".assy");
  }

  @Override
  public boolean isAssignableFrom(Type type)
  { 
    
    if (super.isAssignableFrom(type))
    { return true;
    }
    if (isAggregate() 
        && type.isAggregate()
        && getContentType().isAssignableFrom(type.getContentType())
        )
    { return true;
    }
        
    if (getURI().equals(GENERIC_BUILDER_TYPE_URI)
        && type instanceof BuilderType
        )
    { 
      // All builder types are compatible with the degenerate AssemblyClass
      return true;
    }
    
    if (type instanceof BuilderType
        && getTargetType().isAssignableFrom
          (((BuilderType) type).getTargetType())
        )
    { return true;
    }
    if (debug)
    { log.fine("BuilderType -isAssignableFrom "+this+" : "+type);
    }
    return false;
  }
  
  public static URI canonicalURI(AssemblyClass assemblyClass)
  {
    if (assemblyClass!=null)
    {
      URI baseUri=null;
      if (assemblyClass.getSourceURI()!=null)
      { 
        baseUri=TypeResolver.desuffix(assemblyClass.getSourceURI(),".assy.xml");
      
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
        { return URIPool.create(baseUri.toString()+INNER_PATH_SEPARATOR+path+".assy");
        }
        else
        { return URIPool.create(baseUri.toString()+".assy");
        }
      }
      else if (assemblyClass.getBaseClass()==null
              && assemblyClass.getDefiningClass()==null
              )
      { 
        // No source URI, no base class, no defining class = java class
        try
        {
          Type<?> reflectionType
            =ReflectionType.canonicalType(assemblyClass.getJavaClass());
          if (!reflectionType.isPrimitive())
          {
            // Don't use ReflectionType alternates map.
            return 
              URIPool.create
                ("class:/"
                  +AssemblyClass.classNameToPath
                    (assemblyClass.getJavaClass().getName())
                  +".assy"
                );
          }
          else
          { 
            return
              URIPool.create
                ("class:/"
                  +AssemblyClass.classNameToPath
                  (assemblyClass.getJavaClass().getName())
                );
          }          
        }
        catch (DataException x)
        { throw new RuntimeException(x);
        }
        
        
      }
      else
      { return null;
      }
    }
    else
    { return null;
    }
  }
  
  public static Type<Assembly> canonicalType(AssemblyClass assemblyClass)
    throws DataException
  { return TypeResolver.getTypeResolver().<Assembly>resolve(canonicalURI(assemblyClass));
  }
  
  /**
   * <p>Return the canonical type of the object represented by a
   *   property specifier
   * </p>
   * 
   * @param specifier
   * @return
   * @throws DataException
   */
  public static Type<?> canonicalType(PropertySpecifier specifier)
    throws DataException
  { 
    Class javaClass=null;
    
    try
    { javaClass=specifier.getPropertyType();
    }
    catch (BuildException x)
    { throw new DataException("Error resolving property",x);
    }
    
    try
    {

    
      if (!AssemblyClass.isManaged(javaClass)
          || ReflectionType.isManaged(javaClass)
          )
      { 
      
        return ReflectionType.canonicalType(javaClass);
      }
    
      int arrayDepth=0;
      while (javaClass.isArray())
      { 
        arrayDepth++;
        javaClass=javaClass.getComponentType();
      }

      AssemblyClass assemblyClass
        =AssemblyLoader.getInstance().findAssemblyClass(javaClass);
      if (assemblyClass==null)
      { throw new DataException("No AssemblyClass associated with "+javaClass);
      }
      
      Type<?> ret=canonicalType(assemblyClass);
      while (arrayDepth>0)
      {
        ret=Type.getArrayType(ret);
        arrayDepth--;
      }
      return ret;
      
    }
    catch (BuildException x)
    { throw new DataException("Error finding AssemblyClass for "+javaClass,x);
    }
    
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
    
    URI assemblyURI=TypeResolver.desuffix(uri,".assy");
    try
    {
      targetAssemblyClass
        =AssemblyLoader.getInstance()
          .findAssemblyClass(assemblyURI);
    }
    catch (BuildException x)
    { throw new DataException("Error loading AssemblyClass from "+assemblyURI,x);
    }
    
  }

  AssemblyClass getAssemblyClass()
  { return targetAssemblyClass;
  }

  public Class<?> getTargetType()
  { return targetAssemblyClass.getJavaClass();
  }
  

  
  @Override
  public void link()
  {
    if (linked)
    { return;
    }
    linked=true;
    
    try
    {
      AssemblyClass baseAssemblyClass=targetAssemblyClass.getBaseClass();
      if (baseAssemblyClass!=null)
      { 
        archetype=
          resolver.resolve
            (canonicalURI(baseAssemblyClass)
            );
        
      }
  
      BuilderScheme scheme=new BuilderScheme(this,targetAssemblyClass);
      scheme.addFields();
      this.scheme=scheme;
      super.link();
      classField=scheme.getFieldByName("class");
    }
    catch (DataException x)
    { throw newLinkException(x);
    }

  }

  public Assembly<?> newAssembly(Focus<?> parentFocus)
    throws BuildException
  { return targetAssemblyClass.newInstance(parentFocus);
  }
  
  /**
   * Translate a Tuple describing an Assembly object into
   *   either a new Assembly or an existing one in the
   *   the specified PropertyBinding context.
   */
  @Override
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
          if (classField==null)
          { throw new DataException("No class field in "+this);
          }
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
          if (contents!=null && contents.length>0)
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
        instance=newAssembly(parent!=null?parent.getFocus():null);
//        System.err.println("BuilderType: constructed new instance of "+getURI());
      }
      ((BuilderScheme) scheme).depersistBeanProperties(t,instance);
      return instance;
      
    }
    catch (BuildException x)
    { throw new DataException(x.toString(),x);
    }

  }
  
  @Override
  public DataComposite toData(Assembly<?> obj)
    throws DataException
  {
          
      if (!(obj instanceof Assembly))
      { throw new DataException(obj.getClass().getName()+" is not an Assembly");
      }
    
      Assembly<?> assembly=(Assembly<?>) obj;
    
      if (assembly.getAssemblyClass()!=targetAssemblyClass)
      {   
        // log.fine("Narrowing "+getURI());
        Type<Assembly<?>> targetType
          =(BuilderType) Type.<Assembly<?>>resolve
            (canonicalURI(assembly.getAssemblyClass())
            );
        return targetType.toData(assembly);    
      }
      else
      {
        // log.fine("Not narrowing "+getURI());
        EditableTuple t=new EditableArrayTuple(scheme);
        ((BuilderScheme) scheme).persistBeanProperties( assembly ,t);
        return t;
      }
  }
  
}
