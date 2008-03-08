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

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.data.core.ArrayType;
import spiralcraft.data.core.AbstractCollectionType;
import spiralcraft.data.core.CoreTypeFactory;
import spiralcraft.data.core.MetaType;

import spiralcraft.data.session.BufferType;

import spiralcraft.data.builder.BuilderTypeFactory;

import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.reflect.ReflectionTypeFactory;

import spiralcraft.data.xml.XmlTypeFactory;
import spiralcraft.log.ClassLogger;


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
  private static final ClassLogger log=new ClassLogger(TypeResolver.class);
  
  private static final ClassLoaderLocal<TypeResolver> classLoaderLocal
    =new ClassLoaderLocal<TypeResolver>();
    
  private static final URI TYPE_TYPE_URI 
    =URI.create("java:/spiralcraft/data/Type");
  
  protected final TypeResolver parent;
  private final HashMap<URI,Type<?>> map=new HashMap<URI,Type<?>>();
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

  public final URI getPackageURI(URI typeURI)
  {
    String uriStr=typeURI.toString();
    return URI.create(uriStr.substring(0,uriStr.lastIndexOf('/')+1));
  }
  
  public final Type<?> resolveFromClass(Class<?> clazz)
    throws TypeNotFoundException
  { return resolve(ReflectionType.canonicalURI(clazz));
  }
  
  @SuppressWarnings("unchecked") // Generic method but heterogeneous implementation
  public final <T> Type<T> resolve(URI typeUri)
    throws TypeNotFoundException
  { 
    if (typeUri==null)
    { return null;
    }

//    if (!typeUri.isAbsolute())
//    { 
//      throw new IllegalArgumentException
//        ("Type URI ["+typeUri+"] is relative and cannot be resolved");
//    }
    
    try
    { typeUri=Resolver.getInstance().canonicalize(typeUri);
    }
    catch (UnresolvableURIException x)
    { throw new TypeNotFoundException(typeUri,x);
    }
    
    Type<T> type=null;
    try
    { type=load(typeUri);
    }
    catch (TypeNotFoundException x)
    { throw x;
    }
    catch (DataException x)
    { throw new TypeNotFoundException(typeUri,x);
    }
    
    if (type!=null)
    { 
      // Link was here
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
  public final Type<?> getMetaType()
  { 
    try
    { return resolve(TYPE_TYPE_URI);
    }
    catch (TypeNotFoundException x)
    { 
      throw new RuntimeException
        ("TypeResolver cannot resolve "+TYPE_TYPE_URI,x);
    }
  }

  @SuppressWarnings("unchecked") // Heterogenous ArrayType construction
  private final Type loadArrayType(Type baseType,URI typeURI)
    throws DataException
  {
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    type=new ArrayType(baseType,typeURI);
    type=putMap(typeURI,type);
    return type;
  }
  
  @SuppressWarnings("unchecked") // Heterogeneuous CollectionType construction
  private final Type loadListType(Type baseType,URI typeURI)
    throws DataException
  {
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    // Create and map the list type
    type=new AbstractCollectionType<ArrayList>
      (this
      ,baseType
      ,typeURI
      ,ArrayList.class
      );
    type=putMap(typeURI,type);
    return type;
  }

  private final Type<?> loadBufferType(Type<?> baseType,URI typeURI)
    throws DataException
  {
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    type=new BufferType
      (this
      ,typeURI
      ,baseType
      );
    type=putMap(typeURI,type);
    return type;
  } 

  private final Type<?> loadMetaType(Type<?> baseType,URI typeURI)
    throws DataException
  {
    log.fine("Loading MetaType for "+baseType);
    
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    type=new MetaType
      (this
      ,typeURI
      ,baseType.getURI()
      ,baseType.getClass()
      );
    type=putMap(typeURI,type);
    return type;
  }

  
  @SuppressWarnings("unchecked")
  private final Type findLoadedType(URI typeUri)
    throws DataException
  {
    
    Type type=map.get(typeUri);
    if (type!=null)
    { 
      // System.err.println("TypeResolver- using cached "+typeUri+" = "+type.getURI());
      return type;
    }

    if (typeUri.getPath().endsWith(".array"))
    {
      URI baseTypeUri=desuffix(typeUri,".array");
      Type baseType=findLoadedType(baseTypeUri);
      if (baseType!=null)
      { return loadArrayType(baseType,typeUri);
      }
    }
    else if (typeUri.getPath().endsWith(".list"))
    {
      URI baseTypeUri=desuffix(typeUri,".list");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri);
      if (baseType!=null)
      { return loadListType(baseType,typeUri);
      }
    }
    else if (typeUri.getPath().endsWith(".type"))
    {
      URI baseTypeUri=desuffix(typeUri,".type");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri);
      if (baseType!=null)
      { return loadMetaType(baseType,typeUri);
      }
    }
    else if (typeUri.getPath().endsWith(".buffer"))
    {
      URI baseTypeUri=desuffix(typeUri,".buffer");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri);
      if (baseType!=null)
      { return loadBufferType(baseType,typeUri);
      }
    }
    
    return null;
  }

  /**
   * Register a Type, without linking it
   *
   * @param uri
   * @param type
   * @throws DataException
   */
  public synchronized void register(URI uri,Type<?> type)
    throws DataException
  { 
    if (map.get(uri)!=null)
    { 
      throw new DataException
        ("Type "+uri+" already registered as "+map.get(uri));
    }
    map.put(uri,type);
    log.fine("register "+type);
    // Type may not be ready to be linked
  }
  
  public synchronized void unregister(URI uri,Type<?> type)
  {
    if (map.get(uri)!=type)
    { log.fine("Unregister non-registered type"+type);
    }
    else
    { map.remove(uri);
    }
  }

  private synchronized Type<?> putMap(URI uri,Type<?> type)
    throws DataException
  {
    Type<?> existing=map.get(uri);
    if (existing!=null)
    { 
      if (existing!=type)
      {
        log.fine("NOT remapping type "+type+" from "+existing);
        new Exception("FAILED REMAP").printStackTrace();
      }
      return existing;
      // Pre-register case- don't link, because type is still loading-
      //   factory will call link
    }
    else
    {
      
      // Standard case
      map.put(uri,type);
      type.link();
      if (!type.isLinked())
      { throw new DataException("Link failed silently for "+type);
      }
    }
    return type;
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
      { return loadArrayType(baseType,typeUri);
      }
    }
    else if (typeUri.getPath().endsWith(".list"))
    {
      URI baseTypeUri=desuffix(typeUri,".list");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri);
      if (baseType!=null)
      { return loadListType(baseType,typeUri);
      }
    }
    else if (typeUri.getPath().endsWith(".buffer"))
    {
      URI baseTypeUri=desuffix(typeUri,".buffer");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri);
      if (baseType!=null)
      { return loadBufferType(baseType,typeUri);
      }
    }
    else if (typeUri.getPath().endsWith(".type"))
    {
      URI baseTypeUri=desuffix(typeUri,".type");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri);
      if (baseType!=null)
      { return loadMetaType(baseType,typeUri);
      }
    }
    else
    {
      type=findType(typeUri);
      if (type!=null)
      { 
        // System.err.println("TypeResolver: Caching "+typeUri+" = "+type.getURI());
        type=putMap(typeUri,type);
      }
      return type;
    }
    return null;
  }
  
  /**
   * @return the Type which corresponds to the specified name
   *   within the specified namespace.
   */
  @SuppressWarnings("unchecked") // Types not genericized in dynamic loader
  synchronized final Type load(URI typeUri)
    throws DataException
  { 
    Type<?> type=this.findLoadedType(typeUri);
    if (type!=null)
    { return type;
    }
    
    

    // Delegate to parent
    if (parent!=null)
    {
      type=parent.load(typeUri);
      if (type!=null)
      { 
        type.link();
        return type;
      }
    }
    
    type=findTypeExtended(typeUri);

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
    }
    return type;
  }

  
  /**
   * Called when a Type is not already loaded and needs to be found.
   */
  protected final Type<?> findType(URI typeUri)
    throws DataException
  { 
    Type<?> type=null;
    for (TypeFactory factory: factories)
    { 
      type=factory.createType(this,typeUri);
      if (type!=null)
      { break;
      }
    }
    return type;
  }

  
}