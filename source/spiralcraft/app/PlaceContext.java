//
//Copyright (c) 2012 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app;


import java.io.IOException;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.data.Space;
import spiralcraft.data.access.Schema;
import spiralcraft.data.access.Store;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.lang.Binding;
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.kit.BindingContext;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.meta.Version;
import spiralcraft.security.auth.Authenticator;
import spiralcraft.service.SchedulerService;
import spiralcraft.service.Service;
import spiralcraft.service.ServiceGroup;
import spiralcraft.task.TaskScheduler;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.context.Authority;
import spiralcraft.vfs.context.ContextResourceMap;
import spiralcraft.vfs.context.FileSpace;
import spiralcraft.vfs.context.Graft;

/**
 * <p>Defines a point in the compositional hierarchy of an application
 *   where a new scope of functionality and/or persistence is defined.
 * </p>
 * 
 * @author mike
 *
 */
public class PlaceContext
  extends AbstractChainableContext
  implements Lifecycle
{
  
  private String id;
  private Version version;
  private Space space;
  private Store[] localStores;
  private ServiceGroup serviceGroup;
  private FileSpace fileSpace;
  private Container dataContainer;
  private PluginContext[] pluginContexts;
  private Authority[] authorities;
  private Authority[] exportMounts;
  private ContextResourceMap vfsMappings
    =new ContextResourceMap();
  private PlaceStatus status;
  private SchedulerService<Void> schedulerService;
  private Binding<Void> preInitialize;
  private Binding<Void> preUpgrade;
  private Binding<Void> postInitialize;
  private Binding<Void> postUpgrade;
  private Authenticator authenticator;
  private Binding<?>[] imports;
  private Binding<?>[] exports;
  private boolean hasSpace;
  private Binding<?> afterStart;
  
  public void setId(String id)
  { this.id=id;
  }
  
  public String getId()
  { return id;
  }
  
  public void setImports(Binding<?>[] imports)
  { this.imports=imports;
  }

  public void setExports(Binding<?>[] exports)
  { this.exports=exports;
  }
  
  public void setPlugins(PluginContext[] plugins)
  { this.pluginContexts=plugins;
  }

  public void setMounts(Authority[] authorities)
  { this.authorities=authorities;
  }

  public void setExportMounts(Authority[] authorities)
  { this.exportMounts=authorities;
  }
  
  public void setAuthenticator(Authenticator authenticator)
  { this.authenticator=authenticator;
  }

  public void setSchedulers(TaskScheduler[] schedulers)
  { 
    schedulerService=new SchedulerService<Void>();
    schedulerService.setSchedulers(schedulers);
  
  }
  
  public void setServices(Service[] services)
  { 
    serviceGroup=new ServiceGroup();
    serviceGroup.setServices(services);
  }
  
  public void setVersion(Version version)
  { this.version=version;
  }
  
  public Version getVersion()
  { return version;
  }
  
  public PlaceStatus getStatus()
  { return status;
  }
  
  public void setPreInitialize(Binding<Void> preInitialize)
  { this.preInitialize=preInitialize;
  }

  public void setPreUpgrade(Binding<Void> preUpgrade)
  { this.preUpgrade=preUpgrade;
  }
  
  public void setPostInitialize(Binding<Void> postInitialize)
  { this.postInitialize=postInitialize;
  }

  public void setPostUpgrade(Binding<Void> postUpgrade)
  { this.postUpgrade=postUpgrade;
  }

  public void setAfterStart(Binding<?> afterStart)
  { this.afterStart=afterStart;
  }
  
  public void setStores(Store[] stores)
  { 
    this.localStores=stores;
    hasSpace=true;
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    PlaceContext parent=LangUtil.findInstance(PlaceContext.class,chain);
    if (parent!=null)
    {
      if (id==null)
      { throw new ContextualException
          ("Place does not have an id",declarationInfo);
      }
      try
      { 
        dataContainer=parent.allocatePlaceDataContainer(id);
      }
      catch (IOException x)
      { 
        throw new ContextualException
          ("Error resolving data container for place "+id,declarationInfo,x);
      }
    }
    else
    { 
      hasSpace=true;
      try
      { 
        dataContainer
          =Resolver.getInstance().resolve("context://data/")
            .ensureContainer();
      }
      catch (IOException x)
      { 
        throw new ContextualException
          ("Error resolving root data container context://data/",declarationInfo,x);
      }
    }
    vfsMappings.put("place.data",dataContainer.getURI());
    if (authorities!=null)
    { 
      for (Authority authority:authorities)
      { 
        authority.bind(chain);
        vfsMappings.put(authority);
      }
    }    

    ChainableContext localChain=vfsMappings;
    vfsMappings.setLogLevel(logLevel);
    
    if (imports!=null)
    {
      for (Binding<?> binding:imports)
      { 
        binding.bind(chain);
        chain.addFacet(chain.chain(binding));
      }
    }
    
    if (pluginContexts!=null)
    {
      for (PluginContext pluginContext : pluginContexts)
      { 
        try 
        { 
          log.info("Chaining plugin "+pluginContext.getPluginId());
          chainPlugin(localChain,pluginContext);
        }
        catch (RuntimeException x)
        { 
          throw new ContextualException
            ("Error chaining plugin "+pluginContext.getPluginId(),x);
        }
      }

      
    }

    if (hasSpace)
    {
      space=new Space();
      if (localStores!=null)
      { 
        for (Store store:localStores)
        { registerLocalStore(store);
        }
      }
      localChain.chain(space);
    }
    
    if (authenticator!=null)
    { localChain.chain(authenticator);
    }
    
    fileSpace=new FileSpace();
    fileSpace.setLogLevel(logLevel);
    if (exportMounts!=null)
    {
      for (Authority authority:exportMounts)
      { fileSpace.addAuthority(authority);
      }
    }
    localChain.chain(fileSpace);
    
    if (serviceGroup!=null)
    { localChain.chain(serviceGroup);
    }
    
    if (schedulerService!=null)
    { localChain.chain(schedulerService);
    }
    
    insertNext(localChain);
    
    chain=chain.chain(LangUtil.constantChannel(this));
    
    return chain;
  }
  


  private void registerLocalStore(Store store)
    throws ContextualException
  { 
    String name=store.getName();
    Schema schema=store.getSchema();
    if (name==null && schema!=null)
    { name=schema.getName();
    }
    if (name==null)
    {
      throw new ContextualException
        ("Cannot register nameless Store for place "+id); 
    }
    try
    {
      store.setLocalResourceURI
        (dataContainer.ensureChildContainer(name+".store").getURI());
    }
    catch (IOException x)
    { 
      throw new ContextualException
        ("Error allocating data container for store "
          +name+" in place "+id
        ,x
        );
    }
    store.setName(name);
    registerStore(store);
    
  }
  
  @Override
  public void seal(Contextual last)
  {
    if (exports!=null)
    { chain(new BindingContext(exports));
    }
    super.seal(last);
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> chain)
    throws ContextualException
  { 
    if (exports!=null)
    { 
      for (Binding<?> binding:exports)
      { 
        log.fine("Binding export "+binding);
        binding.bind(chain);
        chain.addFacet(chain.chain(binding));
      }
    }
    if (preInitialize!=null)
    { preInitialize.bind(chain);
    }
    if (preUpgrade!=null)
    { preUpgrade.bind(chain);
    }
    if (postInitialize!=null)
    { postInitialize.bind(chain);
    }
    if (postUpgrade!=null)
    { postUpgrade.bind(chain);
    }
    if (afterStart!=null)
    { afterStart.bind(chain);
    }
    return chain;
  }
  
  void registerStore(Store store)
    throws ContextualException
  { 
    if (space!=null)
    { space.addStore(store);
    }
    else
    { 
      throw new ContextualException
        ("This Place does not have its own data space. "
          +getId()+" trying to register store "+store.getName()
        );
    }
  }
  
  void registerMount(Authority authority)
  { fileSpace.addAuthority(authority);
  }
  
  void publishGraft(Graft graft)
  { 
    Authority defaultTree=fileSpace.getAuthority("");
    if (defaultTree==null)
    { 
      defaultTree=new Authority();
      fileSpace.addAuthority(defaultTree);
    }
    defaultTree.addGraft(graft);
    
  }
  
  private Container allocatePlaceDataContainer(String placeId)
    throws IOException
  { return dataContainer.ensureChildContainer(placeId+".place");
  }
  
  /**
   * Allocates a subdirectory of the Place's data directory for exclusive
   *   use by a plugin to store its own instance data.
   * 
   * @param pluginId The subdirectory will be named [pluginId].plugin
   * @return The Container resource allocated exclusively to the plugin
   * @throws IOException
   */
  Container allocatePluginDataContainer(String pluginId)
    throws IOException
  { return dataContainer.ensureChildContainer(pluginId+".plugin");
  }
  
  private void chainPlugin(ChainableContext localChain,PluginContext pluginContext)
    throws ContextualException
  {
    pluginContext.setPlaceContext(this);
    localChain.chain(pluginContext);
  }
  

  private void resolveStatus()
    throws IOException,ContextualException
  {
    Resource statusXml=dataContainer.getChild("place.status.xml");
    if (statusXml.exists())
    { 
      status=
        ReflectionType.canonicalType(PlaceStatus.class)
          .fromXmlResource(statusXml);

      
      if (status.getId()!=null && !status.getId().equals(id))
      { 
        throw new ContextualException
          ("Starting place id "+id+": Data in "+dataContainer.getURI()
          +" is for a different place id "+status.getId()
          );
      }
      if (status.getVersion()!=null && status.getVersion().compareTo(version)>0)
      {
        throw new ContextualException
          ("Starting place id "+id+": Data in "+dataContainer.getURI()
          +" is for a more recent version ("+status.getVersion()
          +") than the running version ("+version+")"
          );
      
      }
    }
    
  }

  private void preCheckStatus()
  {
    if (status!=null)
    {
      if ( version!=null &&
            (status.getVersion()==null 
              || status.getVersion().compareTo(version)<0
            )
          )
      {
        log.info
          ("Upgrading place "+id+" version "+status.getVersion()
          +" to version "+version
          );
        if (preUpgrade!=null)
        { preUpgrade.get();
        }
      }
    }
    else
    {
      log.info
        ("Initializing place "+id+" version "+version);
      if (preInitialize!=null)
      { preInitialize.get();
      }
    }
  }
  
  private void postCheckStatus()
  {
    if (status!=null)
    {
      if (status.getVersion().compareTo(version)<0)
      {
        log.info
          ("Completing upgrade of place "+id+" version "+status.getVersion()
          +" to version "+version
          );
        if (postUpgrade!=null)
        { postUpgrade.get();
        }
        status.setVersion(version);
      }
    }
    else
    {
      log.info
        ("Completing initialization of place "+id+" version "+version);
      if (postInitialize!=null)
      { postInitialize.get();
      }
      status=new PlaceStatus();
      status.setId(id);
      status.setVersion(version);
    }
  }
  
  private void writeStatus()
    throws IOException,ContextualException
  {
    Resource statusXml=dataContainer.getChild("place.status.xml");

    ReflectionType.canonicalType(PlaceStatus.class)
      .toXmlResource(statusXml,status);
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (logLevel.isDebug())
    { log.fine("Starting place "+id);
    }
    try
    { resolveStatus();
    }
    catch (ContextualException x)
    { throw new LifecycleException("Error starting place "+id,x);
    }
    catch (IOException x)
    { throw new LifecycleException("Error starting place "+id,x);
    }
    
    preCheckStatus();

    Lifecycler.start(authorities);
    fileSpace.start();

    if (space!=null)
    { space.start();
    }
    
    if (serviceGroup!=null)
    { serviceGroup.start();
    }
    
    if (schedulerService!=null)
    { schedulerService.start();
    }
    
    postCheckStatus();
    try
    { writeStatus();
    }
    catch (ContextualException x)
    { throw new LifecycleException("Error starting place "+id,x);
    }
    catch (IOException x)
    { throw new LifecycleException("Error starting place "+id,x);
    }
    
    if (afterStart!=null)
    { afterStart.get();
    }
    if (logLevel.isDebug())
    { log.fine("Done starting place "+id);
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (logLevel.isDebug())
    { log.fine("Stopping place "+id);
    }
    
    if (schedulerService!=null)
    { schedulerService.stop();
    }
    
    if (serviceGroup!=null)
    { serviceGroup.stop();
    }
    
    if (space!=null)
    { space.stop();
    }
    
    fileSpace.stop();
    Lifecycler.stop(authorities);
    
    if (logLevel.isDebug())
    { log.fine("Done stopping place "+id);
    }
    
  }

  @Override
  protected void pushLocal()
  { 
    if (logLevel.isFine())
    { log.fine("Pushing PlaceContext "+getId()+": "+getDeclarationInfo());
    }
  }
  
  @Override
  protected void popLocal()
  { 
    if (logLevel.isFine())
    { log.fine("Popping PlaceContext "+getId()+": "+getDeclarationInfo());
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+id+" "+version;
  }
  
}