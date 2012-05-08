//
package spiralcraft.vfs;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.URIUtil;
import spiralcraft.util.thread.CycleDetector;

/**
 * <p>A Package defines an extensible collection of resources 
 * </p>
 * 
 * <p>A Package may overlay a base Package and provide customized 
 *   versions of some or all of the base Package's resource.
 * <p>
 *   
 * <p>The scope of a Package consists of the resources in the directory
 *   tree rooted at the directory that contains the package.xml file 
 * </p>
 * @author mike
 */
public class Package
{
  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(Package.class);
  private static final Level staticLogLevel
    =ClassLog.getInitialDebugLevel(Package.class,Level.INFO);
  
  private static final HashMap<URI,Package> map
    =new HashMap<URI,Package>();
  private static CycleDetector<Package> cycleDetector
    =new CycleDetector<Package>();
  
  
  public static final synchronized Package fromContainer(Resource container)
    throws ContextualException
  {
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
          // A subdirectory that does not exist is implicitly part of its
          //   parent directory's package
          // log.fine("NX Redirecting to "+container.getParent());
          ret=fromContainer(container.getParent());
        }
        else
        {
          Resource resource=container.asContainer().getChild("package.xml");
          if (staticLogLevel.isFine())
          { log.fine("Checked for "+resource);
          }
          if (resource.exists())
          { 
            ret=ReflectionType.canonicalType(Package.class)
              .fromXmlResource(resource);
            ret.uri=container.getURI();
            if (!ret.base.isAbsolute())
            { ret.base=URIUtil.ensureTrailingSlash(ret.uri).resolve(ret.base);
            }
            ret.base=URIUtil.ensureTrailingSlash
              (Resolver.getInstance().resolve(ret.base).getURI());
            if (staticLogLevel.isConfig())
            { log.fine("Loaded package "+ret.uri+" base="+ret.base);
            }
          }
          else
          { ret=fromContainer(container.getParent());
          }
        }
        map.put(container.getURI(),ret);
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
  
  public Resource baseResource(Resource overlayResource)
    throws IOException
  {
    if (base==null)
    { return null;
    }
    
    URI baseURI=base.resolve(uri.relativize(overlayResource.getURI()));
    if (logLevel.isFine())
    { 
      log.fine("Package in "+uri+" based "+overlayResource.getURI()
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
          { return pkg.searchForBaseResource(baseResource);
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