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
import java.util.HashSet;

import java.lang.ref.WeakReference;


import spiralcraft.util.ClassLoaderLocal;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.StringPool;

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.data.core.ArrayType;
import spiralcraft.data.core.AbstractCollectionType;
import spiralcraft.data.core.DeltaType;
//import spiralcraft.data.core.CoreTypeFactory;
import spiralcraft.data.core.MetaType;

import spiralcraft.data.session.BufferType;

//import spiralcraft.data.builder.BuilderTypeFactory;

import spiralcraft.data.reflect.ReflectionType;
//import spiralcraft.data.reflect.ReflectionTypeFactory;

//import spiralcraft.data.xml.XmlTypeFactory;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;


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
  private static volatile int NEXT_ID=0;
  private static final ClassLog log=ClassLog.getInstance(TypeResolver.class);
  private Level debugLevel
    =ClassLog.getInitialDebugLevel(TypeResolver.class,null);
  
  private static final ClassLoaderLocal<TypeResolver> classLoaderLocal
    =new ClassLoaderLocal<TypeResolver>();
    
  private static final URI TYPE_TYPE_URI 
    =URIPool.create("class:/spiralcraft/data/Type");
  
  protected final TypeResolver parent;
  private final HashMap<URI,Type<?>> map=new HashMap<URI,Type<?>>();
  private final WeakReference<ClassLoader> classLoaderRef;

  private final ArrayList<TypeFactory> factories=new ArrayList<TypeFactory>();
  private final int id=NEXT_ID++;
  
  // XXX This is a temporary fix- any global scheme must be post-delegated
  //   because it may reference a non-global scheme (ie "class"), which
  //   may cause a type reference in the wrong direction
  private final HashSet<String> postDelegatedSchemes
    =new HashSet<String>();
  { postDelegatedSchemes.add("file"); 
  }
  
  public static synchronized final TypeResolver getTypeResolver()
  { 
    TypeResolver resolver=classLoaderLocal.getContextInstance();
    if (resolver==null)
    { 
      resolver=new TypeResolver
        (classLoaderLocal.getParentContextInstance());
      classLoaderLocal.setContextInstance(resolver);
      
    }
    return resolver;
  }
  
  /**
   * Remove the specified suffix from the specified URI
   */
  public static final URI desuffix(URI uri,String suffix)
  { 
    String uriStr=uri.toString();
    return URIPool.create
      (StringPool.INSTANCE.get
        (uriStr.substring(0,uriStr.length()-(suffix.length()))));
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
    
    if (debugLevel.canLog(Level.DEBUG))
    {
      log.debug
        (logMessage("Creating "+getClass().getName()+" for classloader "
        + classLoaderRef.get())
        );
    }
    
    String[] factoryClassNames
      =new String[]
      {"spiralcraft.data.xml.XmlTypeFactory"
      ,"spiralcraft.data.core.CoreTypeFactory"
      ,"spiralcraft.data.builder.BuilderTypeFactory"
      ,"spiralcraft.data.reflect.ReflectionTypeFactory"
      ,"spiralcraft.data.xml.XmlPrototypeFactory"
      };

    for (String className:factoryClassNames)
    { 
      try
      {
        factories.add
          ((TypeFactory) Class.forName
            (className,true,Thread.currentThread().getContextClassLoader())
              .newInstance()
          );
      }
      catch (Exception x)
      { 
        throw new RuntimeDataException
          ("Error loading class factory "+className,x);
      }
    }
  
//  Dynamically load type factories    
//    factories.add(new XmlTypeFactory());
//    factories.add(new CoreTypeFactory());
//    factories.add(new BuilderTypeFactory());
//    factories.add(new ReflectionTypeFactory());

    if (parent==null)
    { getMetaType();
    }
    
  }

  @SuppressWarnings("unchecked")
  public final <X> Type<X> resolveSafeFromClass(Class<?> clazz)
  {
    try
    { return (Type<X>) resolveFromClass(clazz);
    }
    catch (DataException x)
    { 
      throw new RuntimeDataException
        ("Error resolving Type for "+clazz.getName(),x);
    }
  }
  
  public final URI getPackageURI(URI typeURI)
  {
    String uriStr=typeURI.toString();
    return URIPool.create
      (StringPool.INSTANCE.get(uriStr.substring(0,uriStr.lastIndexOf('/')+1)));
  }
  
  public final Type<?> resolveFromClass(Class<?> clazz)
    throws DataException
  { return resolve(ReflectionType.canonicalURI(clazz));
  }
  
  public final <T> Type<T> resolve(URI typeUri)
    throws DataException
  { 
    try
    { return resolve(typeUri,true);
    }
    catch (Throwable x)
    { throw new DataException(toString()+": Error resolving type "+typeUri,x);
    }
  }
  
  @SuppressWarnings("unchecked") // Generic method but heterogeneous implementation
  public final <T> Type<T> resolve(URI typeUri,boolean link)
    throws DataException
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
    type=load(typeUri,link);
    
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
    { 
      Type<?> metaType=resolve(TYPE_TYPE_URI);
      metaType.link();
      return metaType;
    }
    catch (DataException x)
    { 
      throw new RuntimeException
        ("TypeResolver cannot resolve "+TYPE_TYPE_URI,x);
    }
  }

  @SuppressWarnings({"unchecked","rawtypes"}) // Heterogenous ArrayType construction
  
  private final Type loadArrayType(Type baseType,URI typeURI,boolean link)
    throws DataException
  {
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    type=new ArrayType(baseType,typeURI);
    type=putMap(typeURI,type,link);
    return type;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Heterogeneuous CollectionType construction
  private final Type loadListType(Type baseType,URI typeURI,boolean link)
    throws DataException
  {
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    // Create and map the list type
    type=new AbstractCollectionType
      (this
      ,baseType
      ,typeURI
      ,ArrayList.class
      );
    type=putMap(typeURI,type,link);
    return type;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private final Type<?> loadBufferType(Type<?> baseType,URI typeURI,boolean link)
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
    
    Type<?> existingType=map.get(typeURI);
    if (existingType!=null)
    { return existingType;
    }
    type=putMap(typeURI,type,link);
    return type;
  } 

  private final Type<?> loadDeltaType(Type<?> baseType,URI typeURI,boolean link)
    throws DataException
  {
    Type<?> type=map.get(typeURI);
    if (type!=null)
    { return type;
    }

    type=new DeltaType
      (this
      ,typeURI
      ,baseType
      );

    Type<?> existingType=map.get(typeURI);
    if (existingType!=null)
    { return existingType;
    }
    type=putMap(typeURI,type,link);
    if (link)
    { type.link();
    }
    return type;
  } 
  
  private final Type<?> loadMetaType(Type<?> baseType,URI typeURI,boolean link)
    throws DataException
  {
    if (debugLevel.canLog(Level.FINE))
    { log.fine(logMessage("Loading MetaType for "+baseType));
    }
    
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
    type=putMap(typeURI,type,link);
    return type;
  }

  
  @SuppressWarnings({ "rawtypes" })
  private final Type findLoadedType(URI typeUri,boolean link)
    throws DataException
  {
    
    Type type=map.get(typeUri);
    if (type!=null)
    { 
      if (link)
      { type.link();
      }
      // System.err.println("TypeResolver- using cached "+typeUri+" = "+type.getURI());
      return type;
    }

    if (typeUri.getPath().endsWith(".array"))
    {
      URI baseTypeUri=desuffix(typeUri,".array");
      Type baseType=findLoadedType(baseTypeUri,link);
      if (baseType!=null)
      { return loadArrayType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".list"))
    {
      URI baseTypeUri=desuffix(typeUri,".list");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri,link);
      if (baseType!=null)
      { return loadListType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".type"))
    {
      URI baseTypeUri=desuffix(typeUri,".type");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri,link);
      if (baseType!=null)
      { return loadMetaType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".buffer"))
    {
      URI baseTypeUri=desuffix(typeUri,".buffer");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri,link);
      if (baseType!=null)
      { return loadBufferType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".delta"))
    {
      URI baseTypeUri=desuffix(typeUri,".delta");

      // Recurse to resolve baseType
      Type baseType=findLoadedType(baseTypeUri,link);
      if (baseType!=null)
      { return loadDeltaType(baseType,typeUri,link);
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
    
    if (debugLevel.canLog(Level.FINE))
    { 
      log.fine(logMessage("register "+type));
      new Exception().printStackTrace();
    }
    
    // Type may not be ready to be linked
  }
  
  public synchronized void unregister(URI uri,Type<?> type)
  {
    if (map.get(uri)!=type)
    { log.fine(logMessage("Unregister non-registered type"+type));
    }
    else
    { map.remove(uri);
    }
  }

  private synchronized Type<?> putMap(URI uri,Type<?> type,boolean link)
    throws DataException
  {
    Type<?> existing=map.get(uri);
    if (existing!=null)
    { 
      if (existing!=type)
      {
        log.fine(logMessage("NOT remapping type "+type+" from "+existing));
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
      if (link)
      {
        type.link();
        if (!type.isLinked())
        { throw new DataException("Link failed silently for "+type);
        }
      }
    }
    return type;
  }
  
  /**
   * Find a type in the local classloader that is a derivative of another
   *   type (ie. an ArrayType based on the type, or the type's Type)
   */
  @SuppressWarnings("rawtypes")
  private final Type findTypeExtended(URI typeUri,boolean link)
    throws DataException
  {
    Type type=null;
    
    if (typeUri.getPath().endsWith(".array"))
    {
      URI baseTypeUri=desuffix(typeUri,".array");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri,link);
      if (baseType!=null)
      { return loadArrayType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".list"))
    {
      URI baseTypeUri=desuffix(typeUri,".list");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri,link);
      if (baseType!=null)
      { return loadListType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".buffer"))
    {
      URI baseTypeUri=desuffix(typeUri,".buffer");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri,link);
      if (baseType!=null)
      { return loadBufferType(baseType,typeUri,link);
      }
    }
    else if (typeUri.getPath().endsWith(".delta"))
    {
      URI baseTypeUri=desuffix(typeUri,".delta");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri,link);
      if (baseType!=null)
      { return loadDeltaType(baseType,typeUri,link);
      }
    }    
    else if (typeUri.getPath().endsWith(".type"))
    {
      URI baseTypeUri=desuffix(typeUri,".type");

      // Recurse to resolve baseType
      Type baseType=findTypeExtended(baseTypeUri,link);
      if (baseType!=null)
      { return loadMetaType(baseType,typeUri,link);
      }
    }
    else
    {
      type=findType(typeUri);
      if (type!=null)
      { 
        // System.err.println("TypeResolver: Caching "+typeUri+" = "+type.getURI());
        type=putMap(typeUri,type,link);
      }
      return type;
    }
    return null;
  }
  
  /**
   * @return the Type which corresponds to the specified name
   *   within the specified namespace.
   */
  @SuppressWarnings("rawtypes")
  synchronized final Type load(URI typeUri,boolean link)
    throws DataException
  { 
    Type<?> type=this.findLoadedType(typeUri,link);
    if (type!=null)
    { return type;
    }

    boolean postDelegate=postDelegatedSchemes.contains(typeUri.getScheme());
    
    // Delegate to parent
    if (!postDelegate && parent!=null)
    {
      if (debugLevel.canLog(Level.FINE))
      { log.fine(logMessage("Pre-Delegating "+typeUri));
      }
      type=parent.load(typeUri,link);
      if (type!=null)
      { 
        if (link)
        { type.link();
        }
        return type;
      }
    }
    
    if (debugLevel.canLog(Level.FINE))
    { log.fine(logMessage("Cache miss- loading "+typeUri));
    }
    
    
    type=findTypeExtended(typeUri,link);

    if (type!=null)
    { 
      if (link)
      { type.link();
      }
      if (debugLevel.canLog(Level.FINE))
      { log.fine(logMessage("Finished loading "+typeUri));
      }
      return type;
    }
    
    if (postDelegate && parent!=null)
    {
      if (debugLevel.canLog(Level.FINE))
      { log.fine(logMessage("Post-Delegating "+typeUri));
      }
      type=parent.load(typeUri,link);
      if (type!=null)
      { 
        if (link)
        { type.link();
        }
        return type;
      }
    }
    return type;
  }

  public TypeResolver getParent()
  { return parent;
  }
  
  public Type<?>[] getLoadedTypes()
  { return map.values().toArray(new Type<?>[map.values().size()]);
  }

  private String logMessage(String message)
  { return "#"+id+": "+message;
  }
  
  
  /**
   * Called when a Type is not already loaded and needs to be found.
   */
  protected final Type<?> findType(URI typeUri)
    throws DataException
  { 
    
    // 2009-04-14 mike Added fix
    // 
    //   Maintain isolation between TypeResolvers for different
    //   ClassLoaders. The TypeFactories should not be able to see the leaf
    //   ClassLoader or any of its classes or resources, otherwise we will
    //   pollute this ClassLoader with information from the leaf ClassLoader
    //   and break the isolation mechanism.
    //
    final ClassLoader childClassLoader
      =Thread.currentThread().getContextClassLoader();
    
    Thread.currentThread().setContextClassLoader(classLoaderRef.get());
    try
    {
      Type<?> type=null;
      for (TypeFactory factory: factories)
      { 
        type=factory.createType(this,typeUri);
        if (type!=null)
        { 
          if (debugLevel.canLog(Level.TRACE))
          { log.trace(logMessage("Created "+typeUri));
          }
          
          break;
        }
      }
      return type;
    }
    finally
    { Thread.currentThread().setContextClassLoader(childClassLoader);
    }
  }

  
}