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
import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.util.Path;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Package;

public class PathContext
  extends AbstractChainableContext
    implements Lifecycle
{
  
  private Path absolutePath;
  private String absolutePathString;
  private Container contentResource;
  private URI contentBaseURI;
  private URI codeBaseURI;
  private URI defaultCodeBaseURI;
  protected PathContext baseContext;
  private PlaceContext placeContext;
  private Binding<URI> codeX;
  protected PathContextMapping[] pathMappings;
      
  /**
   * The path from the context root
   * 
   * @return
   */
  public Path getAbsolutePath()
  { return absolutePath;
  }
  
  /**
   * The URI that contains the base content resources (publicly accessible
   *   files) for this path.
   * 
   * @param container
   */
  public void setContentBaseURI(URI contentBaseURI)
  { this.contentBaseURI=URIUtil.ensureTrailingSlash(contentBaseURI);
  }
  
  /**
   * The URI that contains the base code resources (non-publicly accessible
   *   files) for this path.
   * 
   * @param container
   */
  public void setCodeBaseURI(URI codeBaseURI)
  { this.codeBaseURI=URIUtil.ensureTrailingSlash(codeBaseURI);
  }
  
  /**
   * Provides a default set of resources and functionality that can be
   *   extended or overridden by this PathContext
   * 
   * @param baseContext
   */
  public void setBaseContext(PathContext baseContext)
  { this.baseContext=baseContext;
  }

  /**
   * Specify a PlaceContext which defines the application model for the
   *   subtree rooted at this PathContext.
   * 
   * @param placeContext
   */
  public void setPlaceContext(PlaceContext placeContext)
  { this.placeContext=placeContext;
  }

  
  public void setCodeX(Binding<URI> codeX)
  { 
    if (codeX!=null)
    { codeX.setTargetType(URI.class);
    }
    this.codeX=codeX;
  }
  
  public void setPathMappings(PathContextMapping[] pathMappings)
  { this.pathMappings=pathMappings;
  }
  
  /**
   * Relativize the given absolute path against the absolute path of
   *   this PathContext.
   *   
   * @param absolutePath
   * @return a path that is relative to this PathContext
   */
  public String relativize(String absolutePath)
  {
    if (!absolutePath.startsWith(absolutePathString))
    { 
      throw new IllegalArgumentException
        (absolutePath+" is not in "+absolutePathString);
    }
    String relativePath=absolutePath.substring(absolutePathString.length());
    return relativePath;
  }
  
  /**
   * Given a path relative to this PathContext,
   *   find the associated content resource.
   * 
   * @param absolutePath
   * @return
   */
  public Resource resolveContent(String relativePath)
    throws IOException
  { 
  
    Resource ret=null;
    if (contentResource!=null)
    { 
      ret=Resolver.getInstance().resolve
        (contentResource.getURI().resolve(relativePath));
      if (ret!=null && ret.exists())
      { return ret;
      }
      else
      { ret=null;
      }
    }
    
    if (ret==null && contentBaseURI!=null)
    {
      ret=Resolver.getInstance().resolve
        (contentBaseURI.resolve(relativePath));
      if (ret!=null && ret.exists())
      { return ret;
      }
      else
      { ret=null;
      }
    }
    
    if (ret==null && baseContext!=null)
    { ret=baseContext.resolveContent(relativePath);
    }
    return ret;
  }

  /**
   * Given a path relative to this PathContext,
   *    find the associated code resource.
   * 
   * @param absolutePath
   * @return
   */
  public Resource resolveCode(String relativePath)
    throws IOException
  { 
  
    Resource ret=null;

    URI effectiveCodeBaseURI=getEffectiveCodeBaseURI();
    if (effectiveCodeBaseURI!=null)
    {
      ret=Resolver.getInstance().resolve
        (getEffectiveCodeBaseURI().resolve(relativePath));
      if (!ret.exists())
      {
        try
        {
          Package pkg
            =Package.fromContainer
              (Resolver.getInstance().resolve(getEffectiveCodeBaseURI()));
          if (pkg!=null)
          { ret=pkg.searchForBaseResource(ret);
          }
        }
        catch (ContextualException x)
        { throw new IOException("Error resolving package",x);
        }
      }
      if (ret!=null && ret.exists())
      { return ret;
      }
      else
      { ret=null;
      }
    }
    
    
    if (ret==null && baseContext!=null)
    { ret=baseContext.resolveCode(relativePath);
    }
    
    if (ret==null && codeX!=null)
    { 
      URI codeURI=codeX.get();
      if (codeURI!=null)
      { 
        if (codeURI.isAbsolute())
        { ret=Resolver.getInstance().resolve(codeURI);
        }
        else
        { 
          codeURI=getEffectiveCodeBaseURI().resolve(codeURI);
          ret=Resolver.getInstance().resolve(codeURI);
        }
      }
    }
    return ret;
  }

  public URI getEffectiveCodeBaseURI()
  { return codeBaseURI!=null?codeBaseURI:defaultCodeBaseURI;
  }
  
  /**
   * Return a URI to the container that provides the context for
   *   the relative path.
   * 
   * @param relativePath
   * @return
   */
  public URI mapRelativePath(String relativePath)
  { return URIUtil.addPathSegment(getEffectiveCodeBaseURI(),relativePath);
  }
  
  /**
   * <p>The path relative to the container context
   * </p>
   * 
   * <p>This is set by the container before this PathContext is initialized
   * </p>
   * 
   * @param path
   */
  public void setAbsolutePath(Path path)
  { 
    this.absolutePath=path.asContainer();
    this.absolutePathString=absolutePath.format("/");
  }
  
  /**
   * The directory that would normally be mapped to this PathContext by the
   *   servlet container. 
   * 
   * @param container
   */
  public void setContentResource(Container container)
  { this.contentResource=container;
  }
  
  public void setDefaultCodeBaseURI(URI defaultCodeBaseURI)
  { this.defaultCodeBaseURI=defaultCodeBaseURI;
  }
  

  public void setParent(PathContext parentContext)
  { // this.parent=parentContext;
  }
  
  @Override
  protected Focus<?> bindPeers(Focus<?> focus)
    throws ContextualException
  { 
    focus=super.bindPeers(focus);
    if (codeX!=null)
    { codeX.bind(focus);
    }
    return focus;
  }

  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    if (baseContext!=null)
    { chain=baseContext.bind(chain);
    }
    chain=chain.chain(LangUtil.constantChannel(this));
    
    if (placeContext!=null)
    { chain(placeContext);
    }
    
    return chain;
  }
  
  @Override
  protected void pushLocal()
  {
    super.pushLocal();
    if (baseContext!=null)
    { baseContext.push();
    }
  }
  
  @Override
  protected void popLocal()
  {
    if (baseContext!=null)
    { baseContext.pop();
    }
    super.popLocal();
    
  }  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (baseContext!=null)
    { baseContext.start();
    }
    if (placeContext!=null)
    { placeContext.start();
    }
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    if (placeContext!=null)
    { placeContext.stop();
    }
    if (baseContext!=null)
    { baseContext.stop();
    }
  }

  @Override
  public String toString()
  { return super.toString()+": path="+absolutePath+", place="+placeContext+", base="+baseContext;
  }  
}
