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
package spiralcraft.data;

import java.net.URI;

import java.util.HashMap;
import java.util.ArrayList;

import java.lang.ref.WeakReference;


import spiralcraft.util.ClassLoaderLocal;


import spiralcraft.data.core.ArrayType;
import spiralcraft.data.core.AbstractCollectionType;
import spiralcraft.data.core.CoreTypeFactory;

import spiralcraft.data.builder.BuilderTypeFactory;

import spiralcraft.data.types.meta.TypeType;

import spiralcraft.data.sax.XmlTypeFactory;

import spiralcraft.data.wrapper.ReflectionTypeFactory;
import spiralcraft.data.wrapper.ReflectionType;

/**
 * Resolves Type references to singleton instances of the Type interface. 
 *
 * A TypeResolver will be established for each classloader in the hierarchy of
 *   classloaders.  This allows classloaders to be unloaded and re-loaded at
 *   runtime.
 *
 * The standard TypeResolver behavior is to delegate to its parent first. If a 
 *   Type is not found in the parent, the next child will be checked and
 *   so-forth.
 *
 * Resolution rules given typeUri:
 *
 * - Load prototype from resource <typeUri>.type.xml
 * - Load Type implementation class <namespace-classpath>.<name>Type
 * - Load reflective Type based on POJO class <namespace-classpath>.<name>
 * - Load generic Scheme based type from <namespace>/<name>.scheme.xml
 * - Defer to UDF loader mechanism (custom scheme:)
 * 
 */
public class TypeResolver
{
  private static final ClassLoaderLocal<TypeResolver> classLoaderLocal
    =new ClassLoaderLocal<TypeResolver>();
    
  private static final URI TYPE_TYPE_URI 
    =URI.create("java:/spiralcraft/data/types/meta/Type");
  
  protected final TypeResolver parent;
  private final HashMap<URI,Type> map=new HashMap<URI,Type>();
  private final WeakReference<ClassLoader> classLoaderRef;

  private final ArrayList<TypeFactory> factories=new ArrayList<TypeFactory>();
  
  public static synchronized final TypeResolver getTypeResolver()
  { 
    TypeResolver resolver=classLoaderLocal.getInstance();
    if (resolver==null)
    { 
      resolver=new TypeResolver
        (classLoaderLocal.getParentInstance());
      classLoaderLocal.setInstance(resolver);
      
    }
    return resolver;
  }
  
  /**
   * Remove the specified suffix from the specified URI
   */
  public static final URI desuffix(URI uri,String suffix)
  { 
    String uriStr=uri.toString();
    return URI.create(uriStr.substring(0,uriStr.length()-(suffix.length())));
  }
  

  
  public TypeResolver()
  { this(getTypeResolver());
  }
  
  TypeResolver(TypeResolver parent)
  { 
    this.parent=parent;
    this.classLoaderRef=new WeakReference<ClassLoader>
      (Thread.currentThread().getContextClassLoader()
      );
      
    
    factories.add(new XmlTypeFactory());
    factories.add(new CoreTypeFactory());
    factories.add(new BuilderTypeFactory());
    factories.add(new ReflectionTypeFactory());
    if (parent==null)
    { getMetaType();
    }
    
  }

  public final Type resolveFromClass(Class clazz)
    throws TypeNotFoundException
  { return resolve(ReflectionType.canonicalURI(clazz));
  }
  
  public final Type resolve(URI typeUri)
    throws TypeNotFoundException
  { 
    if (typeUri==null)
    { return null;
    }
    Type<?> type=null;
    try
    { type=load(typeUri);
    }
    catch (DataException x)
    { x.printStackTrace();
    }
    
    if (type!=null)
    { 
      try
      { type.link();
      }
      catch (TypeNotFoundException x)
      { throw x;
      }
      catch (DataException x)
      { 
        if (x.getCause()!=null
            && x.getCause() instanceof TypeNotFoundException
           )
        { throw (TypeNotFoundException) x.getCause();
        }
        else
        { throw new TypeNotFoundException(typeUri,x);
        }
      }
      return type;
    }
    else
    { throw new TypeNotFoundException(typeUri);
    }
  }
  
  /**
   * Return the ClassLoader associated with this TypeResolver
   */
  public final ClassLoader getClassLoader()
  { return classLoaderRef.get();  
  }

  /**
   * Return the Type that describes a Type
   */
  public final Type getMetaType()
  { 
    try
    { return resolve(TYPE_TYPE_URI);
    }
    catch (TypeNotFoundException x)
    { 
      throw new RuntimeException
        ("TypeResolver cannot resolve "+TYPE_TYPE_URI);
    }
  }

  @SuppressWarnings("unchecked")
  private final Type findLoadedType(URI typeUri)
  {
    
    Type type=map.get(typeUri);
    if (type!=null)
    { return type;
    }

    if (typeUri.getPath().endsWith(".array"))
    {
      
      String uriStr=typeUri.toString();
      URI baseTypeUri=URI.create(uriStr.substring(0,uriStr.length()-6));
      Type baseType=findLoadedType(baseTypeUri);
      if (baseType!=null)
      { 
        // Create and map the array type
        type=new ArrayType(baseType,typeUri);
        map.put(typeUri,type);
        return type;
      }
    }
    // XXX Add meta types and lists here?
    return null;
  }


  /**
   * Find a type in the local classloader that is a derivative of another
   *   type (ie. an ArrayType based on the type, or the type's Type)
   */
  @SuppressWarnings("unchecked")
  private final Type findTypeExtended(URI typeUri)
    throws DataException
  {
    Type type=null;
    if (typeUri.getPath().endsWith(".array"))
    {
      URI baseTypeUri=desuffix(typeUri,".array");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri);
      if (baseType!=null)
      {
        // Create and map the array type
        type=new ArrayType(baseType,typeUri);
        map.put(typeUri,type);
        return type;
      }
    }
    else if (typeUri.getPath().endsWith(".list"))
    {
      URI baseTypeUri=desuffix(typeUri,".list");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri);
      if (baseType!=null)
      {
        // Create and map the array type
        type=new AbstractCollectionType<ArrayList>
          (this
          ,baseType
          ,typeUri
          ,ArrayList.class
          );
        map.put(typeUri,type);
        return type;
      }
    }
    else if (typeUri.getPath().endsWith(".type"))
    {
      URI baseTypeUri=desuffix(typeUri,".type");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri);
      if (baseType!=null)
      {
        // Create and map the types type
        //type=baseType.getMetaType();
        type=new TypeType(this,typeUri,baseTypeUri,baseType.getClass());
        map.put(typeUri,type);
        return type;
      }
    }
    else
    {
      type=findType(typeUri);
      if (type!=null)
      { map.put(typeUri,type);
      }
      return type;
    }
    return null;
  }
  
  /**
   * @return the Type which corresponds to the specified name
   *   within the specified namespace.
   */
  synchronized final Type load(URI typeUri)
    throws DataException
  { 
    Type type=findLoadedType(typeUri);
    if (type!=null)
    { return type;
    }
    

    // Delegate to parent
    if (parent!=null)
    {
      type=parent.load(typeUri);
      if (type!=null)
      { return type;
      }
    }
    
    type=findTypeExtended(typeUri);
    

    return type;
  }

  
  /**
   * Called when a Type is not already loaded and needs to be found.
   */
  protected final Type findType(URI typeUri)
    throws DataException
  { 
    Type type=null;
    for (TypeFactory factory: factories)
    { 
      type=factory.createType(this,typeUri);
      if (type!=null)
      { break;
      }
    }
    return type;
    
    
    //if (type==null)
    //{ type=loadProtoType(typeUri);
    //}
    //if (type==null)
    //{ type=loadTypeFromTypeClass(typeUri);
    //}
    //if (type==null)
    //{ type=loadBuilderType(typeUri);
    //}
    //if (type==null)
    //{ type=loadReflectiveType(typeUri);
    //}
    //return type;
  }
  

  
  /**
   * Indicate whether the URI is resolved through the local classloader
   */
//  private boolean isClassPathScheme(String scheme)
//  {
//    return scheme.equals("java") // Spiralcraft
//      || scheme.equals("classpath") // Cocoon
//      || scheme.equals("class") // common
//      || scheme.equals("class-resource") // GNU
//      ;
//  }
}