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
import spiralcraft.data.access.Schema;
import spiralcraft.data.access.Store;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.context.Authority;
import spiralcraft.vfs.context.ContextResourceMap;
import spiralcraft.vfs.context.Graft;

/**
 * <p>Connects a specific plugin into the application hierarchy.
 * </p>
 * 
 * @author mike
 *
 */
public class PluginContext
  extends AbstractChainableContext
{
 
  private String pluginId;
  private Container dataContainer;
  private Container codeContainer;
  private Store[] stores;
  private Authority[] authorities;
  private String[] fileContainers;
  private PlaceContext placeContext;
  private ContextResourceMap vfsMappings
    =new ContextResourceMap();  
  private Graft[] publishedGrafts;
  
  void setPlaceContext(PlaceContext placeContext)
  { this.placeContext=placeContext;
  }
  
  public String getPluginId()
  { return pluginId;
  }
  
  public void setStores(Store[] stores)
  { this.stores=stores;
  }
  
  public void setMounts(Authority[] authorities)
  { this.authorities=authorities;
  }
  
  /**
   * A list of names for file containers managed by this plugin. Each specified
   *   container name will cause a directory "[name].dir" to be created in
   *   the plugin's data directory, and will be mapped to a VFS authority
   *   "place.[plugin-id].[name]"
   * 
   * @param fileContainers
   */
  public void setFileContainers(String[] fileContainers)
  { this.fileContainers=fileContainers;
  }
  
  /**
   * A set of VFS grafts that will added to the primary/default filetree 
   *   so that this plugin can publish specific data.
   */
  public void setPublishedGrafts(Graft[] publishedGrafts)
  { this.publishedGrafts=publishedGrafts;
  }
  
  public void setPluginId(String pluginId)
  { this.pluginId=pluginId;
  }

  @Override
  public void seal(Contextual last)
  {
    if (vfsMappings!=null)
    { insertNext(vfsMappings);
    }
    super.seal(last);
  }
    
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    if (pluginId==null)
    { throw new ContextualException("pluginId cannot be null",declarationInfo);
    }
    try
    { 
      dataContainer=placeContext.allocatePluginDataContainer(pluginId);
    }
    catch (IOException x)
    { 
      throw new ContextualException
        ("Error allocating data container for plugin "+pluginId,x);
    }
    vfsMappings.put(pluginId+".data",dataContainer.getURI());
    
    try
    { 
      if (declarationInfo!=null)
      {
        Resource codeResource=Resolver.getInstance().resolve
            (URIUtil.trimToPath(declarationInfo.getLocation()));
        
        
        codeContainer=codeResource.getParent().asContainer();
        
        if (logLevel.isConfig())
        { 
          log.config
            ("CodeContainer for plugin "+pluginId+" is "+codeContainer.getURI());
        }
      }
    }
    catch (IOException x)
    { 
      throw new ContextualException
        ("Error allocating code container for plugin "+pluginId,x);
    }
    vfsMappings.put(pluginId+".code",codeContainer.getURI());

    // Don't do any chaining in the bind phase. Must use seal() for this
    //    chain(vfsMappings);
    
    if (stores!=null)
    {
      for (Store store: stores)
      { registerStore(store);
      }
    }
    if (authorities!=null)
    { 
      for (Authority authority:authorities)
      { registerMount(authority);
      }
    }
    if (fileContainers!=null)
    { 
      for (String name:fileContainers)
      { registerFileContainer(name);
      }
    }
    if (publishedGrafts!=null)
    { 
      for (Graft graft:publishedGrafts)
      { placeContext.publishGraft(graft);
      }
    }
    
    return chain;
  }
  
  private void registerMount(Authority authority)
  { placeContext.registerMount(authority);
  }
  
  private void registerFileContainer(String name)
    throws ContextualException
  { 
    try
    {
      Authority authority
        =new Authority
          ("place."+pluginId+"."+name
          ,dataContainer.ensureChildContainer(name+".dir").getURI()
          );

    
      registerMount(authority);
    }
    catch (IOException x)
    { 
      throw new ContextualException
        ("Error allocating file container '"+name+"' in plugin "+pluginId
        ,x
        );
    }
  }
  
  private void registerStore(Store store)
    throws ContextualException
  { 
    String name=store.getName();
    Schema schema=store.getSchema();
    if (name==null && schema!=null)
    { 
      name=schema.getName();
      store.setName(name);
    }
    if (name==null)
    {
      throw new ContextualException
        ("Cannot register nameless Store for plugin "+pluginId); 
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
          +name+" in plugin "+pluginId
        ,x
        );
    }
    store.setName(pluginId+"."+name);
    placeContext.registerStore(store);
    
  }
  
}