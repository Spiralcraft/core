package spiralcraft.vfs.context;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

/**
 * Provides path mappings for an authority in a URI scheme
 * 
 * @author mike
 *
 */
public class Authority
  implements Contextual,Lifecycle
{
  private static final ClassLog log
    =ClassLog.getInstance(Authority.class);
  
  private String authorityName="";
  private URI defaultRoot;
  private Graft[] grafts=new Graft[0];
  
  private HashMap<String,Graft> pathMap;
  
  private Level debugLevel=Level.INFO;
  
  
  public Authority()
  {
  }
  
  Authority(String name,URI root)
  { 
    this.authorityName=name;
    this.defaultRoot=root;
  }
  
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  
  public void setAuthorityName(String name)
  { this.authorityName=name;
  }
  
  public void setRootURI(URI rootURI)
  { this.defaultRoot=rootURI;
  }
  
  public URI getRootURI()
  { return defaultRoot;
  }
  
  public String getAuthorityName()
  { return authorityName;
  }
  
  /**
   * The set of Grafts (path-mapped sub-tree resolvers) associated with
   *   this Authority
   * 
   * @param grafts
   */
  public void setGrafts(Graft[] grafts)
  { this.grafts=grafts;
  }
  
  /**
   * Return the Graft mapped to the specified path
   * 
   * @param relativePath
   */
  public Graft getGraft(String relativePath)
  { 
    if (pathMap!=null)
    { return pathMap.get(relativePath);
    }
    else
    { return null;
    }
  }
  
  void mapPath(String path,Graft graft)
  { 
    if (pathMap==null)
    { pathMap=new HashMap<String,Graft>();
    }
    pathMap.put(path,graft);
    
  }
  
  /**
   *  Resolve a relative path
   */
  Resource resolve(String path)
    throws UnresolvableURIException
  {
    if (debugLevel.canLog(Level.FINE))
    { log.fine("Resolving "+path);
    }    
    
    if (pathMap!=null)
    { 
      for (String mappedPath:pathMap.keySet())
      {
        if (path.startsWith(mappedPath))
        { 
          if (debugLevel.canLog(Level.FINE))
          { 
            log.fine
              ("Root = "+pathMap.get(mappedPath)+", "
              +" resolving "+path.substring(mappedPath.length())
              );
            
          }
          
          try
          {
            return pathMap.get(mappedPath)
              .resolve(new URI(null,path.substring(mappedPath.length()),null));
          }
          catch (URISyntaxException x)
          { throw new IllegalArgumentException
              ("Path ["+path+"] cannot be URI encoded",x);
          }
        }
        else
        { 
          if (debugLevel.canLog(Level.FINE))
          { log.fine("["+path+"] does not start with ["+mappedPath+"]");
          }    
          
        }
      }
    }
    
    try
    {
      if (defaultRoot!=null)
      {
        URI pathURI=new URI(null,path,null);
        
        // Important security check
        if (pathURI.isAbsolute() 
            || pathURI.getAuthority()!=null
            || pathURI.getPath().startsWith("/")
            )
        { throw new UnresolvableURIException(pathURI,"URI is not relative");
        }
        
        return new ContextResource
          (new URI
            ("context"
            ,(authorityName!=null?"//"+authorityName:"")+"/"+path
            ,null
            )
          ,Resolver.getInstance().resolve(defaultRoot.resolve(pathURI))
          );
            
      }
      else
      { return null;
      }
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException
        ("Path ["+path+"] cannot be URI encoded",x);
    }    
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    for (Graft graft:grafts)
    { 
      graft.bind(focusChain);
      mapPath(graft.getVirtualURI().getPath(),graft);
    }
    return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  {
    for (Graft graft:grafts)
    { graft.start();
    }
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    for (Graft graft:grafts)
    { graft.stop();
    }    
  }
}
