package spiralcraft.vfs.context;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.ListMap;
import spiralcraft.util.Path;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
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
  private ListMap<String,Graft> childMap;
  
  private Level debugLevel=Level.INFO;
  
  
  public Authority()
  {
  }
  
  public Authority(String name,URI root)
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
    { return pathMap.get(URIUtil.encodeURIPath(relativePath));
    }
    else
    { return null;
    }
  }
  
  /**
   * Map a path to a graft
   * 
   * @param path
   * @param graft
   */
  public void mapPath(String path,Graft graft)
  { 
    if (pathMap==null)
    { 
      pathMap=new HashMap<String,Graft>();
      childMap=new ListMap<String,Graft>();
    }
    if (path.startsWith("/"))
    { path=path.substring(1);
    }
    pathMap.put(URIUtil.encodeURIPath(path),graft);
    Path parentPath=new Path(path).parentPath();
    String pathStr=null;
    if (parentPath!=null)
    { pathStr=parentPath.toString();
    }   
    else
    { pathStr="";
    }
    // log.fine("Added graft in "+pathStr+" for "+graft);
    
    childMap.add(pathStr,graft);
    
  }
  
  /**
   * Return all grafts contained in the specified parent path
   * 
   * @param parentPath
   * @return
   */
  public Graft[] getGrafts(String parentPath)
  {
    if (childMap!=null)
    { 
      List<Graft> grafts=childMap.get(parentPath);
      return grafts!=null
          ?grafts.toArray(new Graft[grafts.size()])
          :null;
    }
    else
    { return null;
    }
  }
  
  /**
   *  Resolve a relative path. The path is already URI encoded
   */
  Resource resolve(String rawPath)
    throws UnresolvableURIException
  {
    if (debugLevel.canLog(Level.FINE))
    { log.fine("Resolving "+rawPath);
    }    
    
    if (pathMap!=null)
    { 
      for (String mappedPath:pathMap.keySet())
      {
        if (rawPath.startsWith(mappedPath))
        { 
          if (debugLevel.canLog(Level.FINE))
          { 
            log.fine
              ("Root = "+pathMap.get(mappedPath)+", "
              +" resolving "+rawPath.substring(mappedPath.length())
              );
            
          }
          
          try
          {
            return pathMap.get(mappedPath)
              .resolve(URIPool.create(rawPath.substring(mappedPath.length())));
          }
          catch (IllegalArgumentException x)
          { throw new IllegalArgumentException
              ("Path ["+rawPath+"] contains invalid URI characters",x);
          }
        }
        else
        { 
          if (debugLevel.canLog(Level.FINE))
          { log.fine("["+rawPath+"] does not start with ["+mappedPath+"]");
          }    
          
        }
      }
    }
    
    try
    {
      if (defaultRoot!=null)
      {
        URI pathURI;
        try
        { pathURI=URIPool.create(rawPath);
        }
        catch (IllegalArgumentException x)
        { throw new UnresolvableURIException(rawPath,"Invalid syntax in URI",x);
        }
        
        // Important security check
        if (pathURI.isAbsolute() 
            || pathURI.getAuthority()!=null
            || pathURI.getPath().startsWith("/")
            )
        { throw new UnresolvableURIException(pathURI,"URI is not relative");
        }
        
        return new ContextResource
          (URIPool.create
            ("context:"
            +(authorityName!=null?"//"+authorityName:"")
            +"/"+rawPath
            )
          ,Resolver.getInstance().resolve(URIPool.get(defaultRoot.resolve(pathURI)))
          );
            
      }
      else
      { return null;
      }
    }
    catch (IllegalArgumentException x)
    { throw new IllegalArgumentException
        ("Path ["+rawPath+"] contains invalid URI characters",x);
    }    
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
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
  
  @Override
  public String toString()
  { return super.toString()+": "+authorityName+" -> "+defaultRoot+" { "+ArrayUtil.format(grafts,"\r\n,","")+" }";
  }
  
}
