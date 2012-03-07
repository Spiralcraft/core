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
import spiralcraft.data.access.Store;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.context.Authority;
import spiralcraft.vfs.context.ContextResourceMap;
import spiralcraft.vfs.context.FileSpace;

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
  private Space space;
  private FileSpace fileSpace;
  private Container dataContainer;
  private PluginContext[] pluginContexts;
  private Authority[] authorities;
  private ContextResourceMap vfsMappings
    =new ContextResourceMap();
  
  
  public void setPlugins(PluginContext[] plugins)
  { this.pluginContexts=plugins;
  }

  public void setMounts(Authority[] authorities)
  { this.authorities=authorities;
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

    chain(vfsMappings);
    
    if (pluginContexts!=null)
    {
      for (PluginContext pluginContext : pluginContexts)
      { chainPlugin(pluginContext);
      }

        
      fileSpace=new FileSpace();
      chain(fileSpace);

      space=new Space();
      chain(space);
      
    }
    return chain;
  }

  void registerStore(Store store)
  { space.addStore(store);
  }
  
  void registerMount(Authority authority)
  { fileSpace.addAuthority(authority);
  }
  
  private Container allocatePlaceDataContainer(String placeId)
    throws IOException
  { return dataContainer.ensureChildContainer(placeId+".place");
  }
  
  Container allocatePluginDataContainer(String pluginId)
    throws IOException
  { return dataContainer.ensureChildContainer(pluginId+".plugin");
  }
  
  private void chainPlugin(PluginContext pluginContext)
    throws ContextualException
  {
    pluginContext.setPlaceContext(this);
    chain(pluginContext);
  }
  

  @Override
  public void start()
    throws LifecycleException
  { 
    Lifecycler.start(authorities);
    fileSpace.start();
    space.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    space.stop();
    fileSpace.stop();
    Lifecycler.stop(authorities);
  }

  @Override
  public String toString()
  {
    return super.toString()+": vfs="+vfsMappings+", "
      +ArrayUtil.format(pluginContexts,"\r\n,","");
  }
  
}