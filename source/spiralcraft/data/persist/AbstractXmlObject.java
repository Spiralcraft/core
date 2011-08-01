//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.data.persist;

import java.net.URI;

import java.io.IOException;

import org.xml.sax.SAXException;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataException;
import spiralcraft.data.DataComposite;

import spiralcraft.data.builder.BuilderType;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Context;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
//import spiralcraft.log.ClassLog;


import spiralcraft.util.thread.ContextFrame;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

/**
 * <p>A persistent object represented in an XML based portable data format.
 * </p>
 *
 * <p>Persistent objects can have their state saved and restored at runtime.
 * </p>
 *
 * <p>This class provides a useful mechanism to load beans from XML definition
 *   files and optionally save their state back to the file.
 * </p>
 * 
 * <p>If the Treferent class is assignable from 
 *    spiralcraft.lang.Contextual, it will be inserted into the
 *    Focus Chain
 * </p>
 * 
 * <p>If the Treferent class is assignable from spiralcraft.builder.LifeCycle,
 *   this interface will be delegated to the referent object
 * </p>
 * 
 * <p>If the Treferent implements spiralcraft.registry.Registrant, it
 *   will be registered
 * </p>
 * 
 * <p>An instance of a persistent object is tied to its non-volatile 
 *   representation in a storage medium.
 * </p>
 * 
 *
 */
public abstract class AbstractXmlObject<Treferent,Tcontainer>
  implements 
    PersistentReference<Treferent>
    ,Lifecycle
    ,Context
{

//
//  private static final ClassLog log
//    =ClassLog.getInstance(AbstractXmlObject.class);
  
  public static final URI typeFromClass(Class<?> clazz)
  { return ReflectionType.canonicalURI(clazz);
  }
  
  
  /**
   * <p>Instantiate the object referenced by the specified URI. The returned 
   *   reference will not be bound or started.
   * </p>
   * 
   * @param <T> The Java generic type of object that is being referred to
   * @param uri A URI referencing either a Type or a resource that contains
   *   instance data.
   * @return The AbstractXmlObject that references the instance.
   * @throws BindException
   * 
   */ 
  public static final <T> AbstractXmlObject<T,?> instantiate
    (URI uri)
    throws BindException
  { 
    URI typeURI=null;
    URI instanceURI=null;
    try
    {
      Type.<T>resolve(uri);
      typeURI=uri;
    }
    catch (TypeNotFoundException x)
    { instanceURI=uri;
    }
    catch (DataException x)
    { throw new BindException("Error loading referenced type "+uri,x);
    }
    return create(typeURI,instanceURI);
  }
  
  /**
   * <p>Create a new AbstractXmlObject appropriate for the specified Type,
   *   read from the optional instanceURI. The returned reference will not
   *   be registered or started.
   * </p>
   * 
   * @param <T> The Java generic type of object that is being referred to
   * @param typeURI The spiralcraft.data.Type of the object being referred to
   * @param instanceURI The URI of the resource from which to read the instance
   * @return The AbstractXmlObject
   * @throws BindException
   * 
   */   
  public static final <T> AbstractXmlObject<T,?> create
    (URI typeURI,URI instanceURI)
    throws BindException
  {  
    AbstractXmlObject<T,?> reference;
    Type<?> type=null;

    
    try
    { 
      if (typeURI!=null)
      { type=Type.resolve(typeURI);
      }
    }
    catch (DataException x)
    { throw new BindException("Type "+typeURI+" could not be resolved",x);
    }
    
    if (type!=null && type instanceof BuilderType)
    { 
      try
      { reference=new XmlAssembly<T>(type.getURI(),instanceURI);
      }
      catch (PersistenceException x)
      { throw new BindException("Error creating XmlAssembly: "+x,x);
      }
    }
    else
    {
      try
      { reference=new XmlBean<T>(type!=null?type.getURI():null,instanceURI);
      }
      catch (PersistenceException x)
      { throw new BindException("Error creating XmlBean",x);
      }
    }
    return reference;
  }
  
  /**
   * <p>Create a new AbstractXmlObject appropriate for the specified Type,
   *   read from the optional instanceURI.
   * </p>
   * 
   * <p>The AbstractXmlObject will be registered and started, where applicable.
   * </p>
   * 
   * <p>If no RegistryNode is specified, and the object is a Registrant,  
   *   the object will be registered with the local root registry node.
   * </p>
   * 
   * <p>If the object implements the Contextual interface, it will
   *   be bound onto the Focus chain at the Focus that references the object
   *   itself.
   * </p>
   * 
   * @param <T> The Java generic type of object that is being referred to
   * @param typeURI The spiralcraft.data.Type of the object being referred to
   * @param instanceURI The URI of the resource from which to read the instance
   * @param registryNode The RegistryNode under which to register this object
   * @return The AbstractXmlObject
   * @throws BindException
   */
  public static final <T> AbstractXmlObject<T,?> activate
    (URI referenceURI
    ,Focus<?> focus
    )
    throws ContextualException
  {
    AbstractXmlObject<T,?> instance=instantiate(referenceURI);
    try
    { 
      if (focus!=null)
      { instance.bind(focus);
      }
      instance.start();
    }
    catch (LifecycleException x)
    { throw new BindException("Error starting Xml object",x);
    }
    return instance;    
  }
  
  
  /**
   * <p>Create a new AbstractXmlObject appropriate for the specified Type,
   *   read from the optional instanceURI.
   * </p>
   * 
   * <p>The AbstractXmlObject will be registered and started, where applicable.
   * </p>
   * 
   * <p>If no RegistryNode is specified, and the object is a Registrant,  
   *   the object will be registered with the local root registry node.
   * </p>
   * 
   * <p>If the object implements the Contextual interface, it will
   *   be bound onto the Focus chain at the Focus that references the object
   *   itself.
   * </p>
   * 
   * @param <T> The Java generic type of object that is being referred to
   * @param typeURI The spiralcraft.data.Type of the object being referred to
   * @param instanceURI The URI of the resource from which to read the instance
   * @param registryNode The RegistryNode under which to register this object
   * @return The AbstractXmlObject
   * @throws BindException
   */
  public static final <T> AbstractXmlObject<T,?> activate
    (URI typeURI
    ,URI instanceURI
    ,Focus<?> focus
    )
    throws ContextualException
  {
    AbstractXmlObject<T,?> reference;
    Type<?> type=null;

    
    try
    { 
      if (typeURI!=null)
      { type=Type.resolve(typeURI);
      }
    }
    catch (DataException x)
    { throw new BindException("Type "+typeURI+" could not be resolved",x);
    }
    
    if (type!=null && type instanceof BuilderType)
    { 
      try
      { reference=new XmlAssembly<T>(type.getURI(),instanceURI);
      }
      catch (PersistenceException x)
      { throw new BindException("Error creating XmlAssembly: "+x,x);
      }
    }
    else
    {
      try
      { reference=new XmlBean<T>(type!=null?type.getURI():null,instanceURI);
      }
      catch (PersistenceException x)
      { throw new BindException("Error creating XmlBean",x);
      }
    }
    
    try
    { 
      if (focus!=null)
      { reference.bind(focus);
      }
      reference.start();
    }
    catch (LifecycleException x)
    { throw new BindException("Error starting Xml object",x);
    }
    return reference;
    
  }

  
  protected URI instanceURI;
  protected URI typeURI;
  protected Type<Tcontainer> type;
  protected Tcontainer instance;
  private Channel<Treferent> channel;
  protected Focus<?> focus;
  protected ContextFrame next;
  protected boolean threaded;
  
  /**
   * Construct an XmlObject from the given Type resource and instance
   *   data resource.
   * 
   * If the typeURI is not specifed, the instanceURI must be specified. The Type will
   *   be read from the data resource.
   *   
   * If the instanceURI is not specified, or does not exist, the typeURI must be
   *   specified. An appropriate object will be instantiated as per the specified Type.
   *   If the instanceURI is specifed but does not exist, the save() method will create
   *   it and store persistent properties from the object.
   *   
   * The subclass extending this should call load() once construction is complete.
   */
  protected AbstractXmlObject(URI typeURI,URI instanceURI)
  { 
    this.typeURI=typeURI;
    this.instanceURI=instanceURI;

  }
  
  @Override
  public void setResourceUri(URI instanceURI)
  { this.instanceURI=instanceURI;
  }
  
  /**
   * Verify that the Type is appropriate for a given implementation
   */
  protected abstract void verifyType(Type<?> type)
    throws DataException;
  
  /**
   * Create a new default instance of the specified type
   */
  protected abstract Tcontainer newInstance()
    throws DataException;

  /**
   *@return The Java object referred to and activated by this XmlObject
   */
  @Override
  public abstract Treferent get();

  @SuppressWarnings({ "unchecked", "rawtypes" }) // Narrowing from Assembly to Assembly<T>
  public void load()
    throws PersistenceException
  {
    try
    { 
      if (typeURI!=null)
      {
        type=TypeResolver.getTypeResolver().<Tcontainer>resolve(typeURI);
        verifyType(type);
        
        if (instanceURI==null)
        { instance = newInstance();
        }
      }

      if (instanceURI!=null)
      {
        DataReader reader=new DataReader();
        Resource resource=Resolver.getInstance().resolve(instanceURI);
        
        // XXX This should be an option
        // reader.setContextAware(true);
        
        if (!resource.exists())
        { 
          if (typeURI!=null)
          {
            try
            { instance=newInstance();
            }
            catch (DataException x)
            {  
              throw new DataException
                ("Resource '"+resource.getURI()+"' does not exist, and a new " +
                   type.getNativeClass().getName() +" could not be created"
                ,x
                );
            }
          }
          else
          { 
            throw new DataException
              ("Resource '"+resource.getURI()+"' does not exist");
          }
        }
        else
        {
        
          DataComposite composite = (DataComposite) reader.readFromResource
            (resource
            ,type
            );

          Type actualType=composite.getType();
          verifyType(actualType);

          instance = (Tcontainer) actualType.fromData(composite,null);
          type=actualType; 
        }
      }
    }
    catch (DataException x)
    { 
      throw new PersistenceException
        ("Error instantiating XmlObject: "+x.toString()
        ,x
        );
    }
    catch (SAXException x)
    { 
      throw new PersistenceException
        ("Error parsing "+instanceURI+": "+x.toString()
        ,x
        );
    }
    catch (IOException x)
    {
      throw new PersistenceException
        ("Error parsing "+instanceURI+": "+x.toString()
        ,x
        );
    }
    
  }

  @Override
  public void save()
    throws PersistenceException
  { 
    if (instanceURI!=null && type!=null)
    {
      DataWriter writer=new DataWriter();
      try
      {
        // Re-evaluate type?
        Tuple tuple=(Tuple) type.toData(instance);
        writer.writeToURI(instanceURI,tuple);
      }
      catch (IOException x)
      { throw new PersistenceException("Error writing "+instanceURI+": "+x,x);
      }
      catch (DataException x)
      { throw new PersistenceException("Error writing "+instanceURI+": "+x,x);
      }
    }
    
  }
  
  
  @Override
  public void start()
    throws LifecycleException
  {
    if (instance instanceof Lifecycle)
    { ((Lifecycle) instance).start();
    }
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    if (instance instanceof Lifecycle)
    { ((Lifecycle) instance).stop();
    }
  }
  

  /**
   * <p>Return the Focus created by this XmlObject as a result of binding.
   * </p>
   * 
   * @return
   */
  public Focus<?> getFocus()
  { return focus;
  }
  
  @Override
  public Focus<?> bind(final Focus<?> parentFocus)
    throws ContextualException
  { 
    // XXX Here, we insert the contained object as a bean into the Focus
    //   chain using this container as a writable store. This allows
    //   the client object to weave the contained object into the
    //   Focus chain regardless of whether it is a Contextual or not.
    //
    //   On the other hand, if the instance -is- a Contextual, this
    //   is probably redundant and we lose the ability to control the
    //   immediate parent context of the Contextual.
    
    Treferent endInstance=get();
    if (endInstance instanceof Contextual)
    { 
      // The Contextual controls what goes into the chain
      this.focus=((Contextual) endInstance).bind(parentFocus);
    }
    else
    {
      // Weave the dumb object into the chain
      if (channel==null)
      { 
        channel=new AbstractChannel<Treferent>
          (BeanReflector.<Treferent>getInstance(get().getClass()))
        {
          { setContext(parentFocus);
          }
          

          @Override
          protected Treferent retrieve()
          { return AbstractXmlObject.this.get();
          }

          @Override
          protected boolean store(
            Treferent val)
            throws AccessException
          { 
            AbstractXmlObject.this.set(val);
            return true;

          }
        
          @Override
          public boolean isWritable()
          { return true;
          }
        };
      }
    
      SimpleFocus<Treferent> intermediateFocus
        =new SimpleFocus<Treferent>(parentFocus,channel);

      this.focus=intermediateFocus;    
    }
    
    if (endInstance instanceof Context)
    { threaded=true;
    }
    return getFocus();
      
  }
  

  @Override
  public void push()
  {
    if (threaded)
    { ((Context) get()).push();
    }
  }
  
  @Override
  public void pop()
  {
    if (threaded)
    { ((Context) get()).pop();
    }
  }

}
