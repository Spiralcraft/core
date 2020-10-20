//
package spiralcraft.vfs;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.thread.CycleDetector;
import spiralcraft.vfs.ovl.OverlayResource;

/**
 * <p>A VFS Package identifies a directory node in a virtual filesystem that
 *   imports and extends a base filesystem.
 * </p>
 * 
 * <p>The scope of a Package consists of the resources in the directory
 *   tree rooted at the directory that contains the package.xml file 
 * </p>
 * 
 * @author mike
 */
public class Package
{
  private static final ClassLog log
    =ClassLog.getInstance(Package.class);
  private static final Level staticLogLevel
    =ClassLog.getInitialDebugLevel(Package.class,Level.INFO);
  
  private static final HashMap<URI,Package> map
    =new HashMap<URI,Package>();
  private static CycleDetector<Package> cycleDetector
    =new CycleDetector<Package>();
  
  
  public static final Resource findResource(String uri)
    throws IOException
  { return findResource(URIPool.create(uri));
  }
  
  public static final Resource findResource(URI uri)
    throws IOException
  { return findResource(Resolver.getInstance().resolve(uri));
  }
  
  /**
   * Given a resource path, find a resource that exists in that path, or if
   *   it cannot be found, search in the packages scoped to this path
   * 
   * @param overlay
   * @return
   * @throws IOException
   */
  public static final Resource findResource(Resource overlay)
    throws IOException
  { 
    if (!overlay.exists())
    { 
      Package pkg=null;
      try
      {
        pkg = Package.fromContainer
          (overlay.getParent());
      }
      catch (ContextualException x)
      { 
        log.log
          (Level.WARNING
          ,"Error resolving package in "+overlay.getParent().getURI()
          ,x
          );
      }
      if (pkg!=null)
      { overlay=pkg.searchForBaseResource(overlay);
      }
      else
      { overlay=null;
      }
    }  
      
    return overlay;
  }
  
  /**
   * Find a resource within the packages scoped to this path
   * 
   * @param overlay
   * @return
   */
  public static final Resource findBaseResource(Resource overlay)
    throws IOException
  {
    Package pkg=null;
    try
    {
      pkg = Package.fromContainer
        (overlay.getParent());
    }
    catch (ContextualException x)
    { 
      log.log
        (Level.WARNING
        ,"Error resolving package in "+overlay.getParent().getURI()
        ,x
        );
    }
    if (pkg!=null)
    { return pkg.searchForBaseResource(overlay);
    }
    return null;
  }

  /**
   * Get a reference to the package defined in this container, if any
   * 
   * @param container
   * @return
   */
  public static final synchronized Package fromThisContainer(Resource container)
    throws ContextualException
  { return fromContainer(container,false);
  }
  
  /**
   * Search for the closest containing package in the specified container
   *   or any of its ancestors.
   * 
   * @param container
   * @return
   * @throws ContextualException
   */
  public static final synchronized Package fromContainer(Resource container)
      throws ContextualException
  { return fromContainer(container,true);
  }
  
  /**
   * Search for the closest containing package in the specified container
   *   or any of its ancestors (if searchParents = true)
   * 
   * @param container
   * @return
   * @throws ContextualException
   */
  public static final synchronized Package fromContainer(Resource container,boolean searchParents)
    throws ContextualException
  {
    while (container instanceof OverlayResource)
    { 
      log.fine("Unpacking overlay "+container);
      container=((OverlayResource) container).getOverlay();
    }
    if (staticLogLevel.isFine())
    { log.fine("Checking for package in "+container);
    }
    if (container==null)
    { return null;
    }
    Package ret=map.get(container.getURI());
    if (ret==null && !map.containsKey(container.getURI()))
    { 
      try
      {        
        if (!container.exists())
        { 
          if (searchParents)
          {
            // A subdirectory that does not exist is implicitly part of its
            //   parent directory's package
            // log.fine("NX Redirecting to "+container.getParent());
            ret=fromContainer(container.getParent(),true);
          }
        }
        else
        {
          Container dir=container.asContainer();
          if (dir!=null)
          {
            Resource packageXml=dir.getChild("package.xml");
            if (staticLogLevel.isFine())
            { log.fine("Checked for "+packageXml);
            }
            if (packageXml.exists())
            { 
              ret=loadPackage(container,packageXml);
            }
            else
            { 
              if (searchParents)
              { 
                ret=fromContainer(container.getParent(),true);
              }
            }
          }
          else
          { log.warning("Resource "+container.getURI()+" is not a container");
          }
        }
        // Don't map a null if we didn't search parents
        if (searchParents || ret!=null)
        {  map.put(container.getURI(),ret);
        }
      }
      catch (IOException x)
      { throw new ContextualException("Error reading package",x);
      }
      catch (DataException x)
      { throw new ContextualException("Error reading package",x);
      }
    }
    return ret;
  }
  
  private static Package loadPackage(Resource container,Resource packageXml)
    throws IOException,ContextualException
  {
    Package ret=ReflectionType.canonicalType(Package.class)
        .fromXmlResource(packageXml);
      ret.uri=container.getURI();
      if (!ret.base.isAbsolute())
      { ret.base=URIUtil.ensureTrailingSlash(ret.uri).resolve(ret.base);
      }
      ret.base=URIUtil.ensureTrailingSlash
        (Resolver.getInstance().resolve(ret.base).getURI());
      if (staticLogLevel.isConfig())
      { log.fine("Loaded package "+ret.uri+" base="+ret.base);
      }
    return ret;
  }
  
  private Level logLevel
    =ClassLog.getInitialDebugLevel(Package.class,Level.INFO);
  
  private URI uri;
  private URI base;
  
  public URI getBase()
  { return base;
  }
  
  public void setBase(URI base)
    throws IOException
  { 
    this.base=base;
  }

  public void setLogLevel(Level logLevel)
  { this.logLevel=logLevel;
  }
  
  /**
   * Map the overlay resource path to a path within this package's container
   * 
   * @param overlayResource
   * @return
   * @throws IOException
   */
  public Resource baseResource(Resource overlayResource)
    throws IOException
  {
    if (base==null)
    { return null;
    }
    
    URI relativeURI=uri.relativize(overlayResource.getURI());
    
    URI baseURI=base.resolve(relativeURI);
    if (logLevel.isFine())
    { 
      log.fine("Package in "+uri+" (base="+base+") based "+relativeURI+" (from "+overlayResource.getURI()+")"
        +" to "+baseURI);
    }
    if (baseURI!=null)
    { return Resolver.getInstance().resolve(baseURI);
    }
    else
    { return null;
    }
  }
  
  public Resource searchForBaseResource(Resource overlayResource)
      throws IOException
  { return searchForBaseResource(overlayResource,null);
  }
      
  /**
   * Map a resource path into the nearest containing package that can provide
   *   an existing resource
   * 
   * @param overlayResource
   * @param searchedPath Intermediate URIs searched
   * @return
   * @throws IOException
   */
  public Resource searchForBaseResource(Resource overlayResource,LinkedList<URI> searchedPath)
    throws IOException
  {
    if (cycleDetector.detectOrPush(this))
    { return null;
    }
    try
    { 
      
      Resource baseResource=baseResource(overlayResource);
      if (logLevel.isFine())
      { log.fine("Searching for "+baseResource);
      }
      if (baseResource!=null)
      { 
        if (baseResource.exists())
        { return baseResource;
        }
        else
        { 
          Resource container=baseResource.getParent();
          Package pkg=null;
          try
          { pkg=Package.fromContainer(container);
          }
          catch (ContextualException x)
          { log.log(Level.WARNING,"Error reading package in "+container.getURI(),x);
          }
          if (pkg!=null)
          { 
            if (searchedPath!=null)
            { searchedPath.add(baseResource.getResolvedURI());
            }
            return pkg.searchForBaseResource(baseResource,searchedPath);
          }
          else
          { return null;
          }
        }
      }
      else
      { return null;
      }
    }
    finally
    { cycleDetector.pop();
    }
  }
  
}
