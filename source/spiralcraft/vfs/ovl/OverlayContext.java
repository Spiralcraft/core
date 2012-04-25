package spiralcraft.vfs.ovl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.thread.ThreadLocalStack;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.context.ContextResourceMap;

/**
 * <p>Maps overlay resource authority names to overlay resources in a 
 *   thread-contextual manner
 * </p>
 * 
 * @author mike
 *
 */
public class OverlayContext
  extends AbstractChainableContext
{
  private static final ClassLog log
    =ClassLog.getInstance(ContextResourceMap.class);
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(ContextResourceMap.class,null);
  
  private static final ThreadLocalStack<OverlayContext> threadStack
    =new ThreadLocalStack<OverlayContext>(true);

  static 
  { new OverlayContext().push();
  }
  
  
  
  
  public static final Resource resolve(URI contextURI)
    throws UnresolvableURIException
  { 
    if (debugLevel.isTrace())
    { log.trace("Resolving "+contextURI);
    }
    Resource ret=threadStack.get().doResolve(contextURI);
    if (ret!=null)
    { return ret;
    }
    else
    { 
      
      throw new UnresolvableURIException
        (contextURI
        ,"Could not resolve "+contextURI+" : mappings="
        +threadStack.get().computeMappings()
        );
    }
  }
  

  private OverlayContext parent;
  private final HashMap<String,Resource> map
    =new HashMap<String,Resource>();
  
  private void setParent(OverlayContext parent)
  {
    if (this.parent==null)
    { this.parent=parent;
    }
    else
    { throw new IllegalStateException("Cannot change parent of "+this);
    }
  }
  
  
  public static OverlayContext get()
  { return threadStack.get();
  }
  
  @Override
  protected void pushLocal()
  { threadStack.push(this);
  }
  
  @Override
  protected void popLocal()
  { threadStack.pop();
  }
  
  LinkedHashSet<URI> computeMappings()
  { 
    LinkedHashSet<URI> set
      =parent!=null
      ?parent.computeMappings()
      :new LinkedHashSet<URI>();
      
    for (String name:map.keySet())
    { 
      if (name.equals(""))
      { set.add(URI.create("ovl:/"));
      }
      else
      { set.add(URI.create("ovl://"+name));
      }
    }
    return set;
  }  

  public void put(String authority,Resource overlay)
  {
    map.put
      (authority
      ,OverlayResource.wrap(overlay)
      );
  }
  
    
  public void put(String authority,Resource overlay,Resource base)
  { 
    try
    {
      map.put
        (authority
        ,new OverlayResource
          (new URI("ovl",authority,"/",null,null)
          ,overlay
          ,base
          )
        );
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x);
    }
  }

  public void put(String authority,URI overlay,URI base)
    throws UnresolvableURIException
  { 
    try
    {
      map.put
        (authority
        ,new OverlayResource
          (new URI("ovl",authority,"/",null,null)
          ,Resolver.getInstance().resolve(overlay)
          ,Resolver.getInstance().resolve(base)
          )
        );
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x);
    }
  }
    
  private Resource doResolve(URI contextURI)
    throws UnresolvableURIException
  {
    if (contextURI.getPath().length()==0)
    { throw new UnresolvableURIException(contextURI,"URI has no path");
    }
    String path=contextURI.getPath().substring(1);
    String authorityName=contextURI.getAuthority();
    if (authorityName!=null && !contextURI.isAbsolute())
    { throw new UnresolvableURIException(contextURI,"Absolute URI has no scheme");
    }

    Resource root;
    if (authorityName!=null)
    { 
      root=map.get(authorityName);
      if (root==null && parent==null)
      { return null;
      }
    }
    else
    { 
      root=map.get("");
      if (root==null && parent==null)
      { return null;
      }
    }

    if (root==null)
    { return parent.doResolve(contextURI);
    }

    Resource ret=root;
    for (String segment:path.split("/"))
    { ret=ret.asContainer().getChild(segment);
    }
    
    return ret;
  }    
  
  @Override
  public String toString()
  { return super.toString()+": "+map.toString()+" parent="+parent;
  }


  @Override
  public Focus<?> bindImports(
    Focus<?> focusChain)
    throws BindException
  { 
    setParent(OverlayContext.get());
    return focusChain;
  }  
}
