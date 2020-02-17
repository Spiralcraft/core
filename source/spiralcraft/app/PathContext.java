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
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.kit.BindingContext;
import spiralcraft.lang.kit.ConstantChannel;
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
  private Boolean publishContent;
  private Boolean onlyPublishMappedContent;
  private Binding<Boolean> guardX;
  private Binding<?>[] imports;
  private Binding<?>[] exports;
  private Binding<?>[] constantImports;
  private Binding<?>[] constantExports;
  private Binding<?>[] onEnter;
  private Binding<?>[] onExit;
      
  /**
   * The path from the context root
   * 
   * @return
   */
  public Path getAbsolutePath()
  { return absolutePath;
  }
  
  public String getAbsolutePathString()
  { return absolutePathString;
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
   * An optional condition to allow access to resources in this path and 
   *   subpaths
   * 
   * @param guardX
   */
  public void setGuardX(Binding<Boolean> guardX)
  { this.guardX=guardX;
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

  public void setPublishContent(boolean publishContent)
  { this.publishContent=publishContent;
  }
  
  /**
   * Specify that only content mapped via the contentBaseURI should be published as
   *   opposed to resources in the subtree where this PathContext is defined
   * 
   * @param onlyPublishMappedContent
   */
  public void setOnlyPublishMappedContent(boolean onlyPublishMappedContent)
  { this.onlyPublishMappedContent=onlyPublishMappedContent;
  }

  /**
   * Evaluate an expression after this PathContext is entered
   * 
   * @param onEnter
   */
  public void setOnEnter(Binding<?>[] onEnter)
  { this.onEnter=onEnter;
  }
  
  /**
   * Evaluate an expression before this PathContext is exited
   * 
   * @param onEnter
   */
  public void setOnExit(Binding<?>[] onExit)
  { this.onExit=onExit;
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
   * Bindings that will be evaluated before this PathContext is bound, with
   *   the resulting objects published in the focus chain and made available to
   *   code internal to this PathContext and to any referenced PlaceContext
   * 
   * @param imports
   */
  public void setConstantImports(Binding<?>[] imports)
  { this.constantImports=imports;
  }

  /**
   * Bindings that will be available to code internal to this PathContext
   *   and to any referenced PlaceContext
   * 
   * @param imports
   */
  public void setImports(Binding<?>[] imports)
  { this.imports=imports;
  }

  /**
   * 
   * Bindings that will be evaluated after this PathContext is bound, with
   *   the resulting objects published in the focus chain and made available to
   *   code running under this PathContext
   * 
   * 
   * @param exports
   */
  public void setConstantExports(Binding<?>[] exports)
  { this.constantExports=exports;
  }

  /**
   * Bindings that will be available to code running under this PathContext
   * 
   * @param exports
   */
  public void setExports(Binding<?>[] exports)
  { this.exports=exports;
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
  
    if (Boolean.FALSE.equals(publishContent))
    { return null;
    }
    else if (publishContent==null)
    {
      if (baseContext!=null)
      { return baseContext.resolveContent(relativePath);
      }
      else
      { return null;
      }
    }
    
    Resource ret=null;
    if (contentResource!=null
        && !Boolean.TRUE.equals(onlyPublishMappedContent) 
        )
    { 
      ret=Resolver.getInstance().resolve
        (contentResource.getURI().resolve(relativePath));
      if (ret!=null && ret.exists())
      { 
        if (logLevel.isFine())
        { 
          log.fine("Resolved content '"+relativePath
            +"' in contentResource "+contentResource.getURI()
            );
        }
        return ret;
      }
      else
      { ret=null;
      }
    }
    
    if (ret==null && contentBaseURI!=null)
    {
      URI uri=contentBaseURI.resolve(relativePath);
      ret=Resolver.getInstance().resolve(uri);
      if (logLevel.isFine())
      { log.fine(contentBaseURI+ " + "+relativePath+" = "+uri+" -> "+ret); 
      }
      
      if (ret!=null && ret.exists())
      { 
        if (logLevel.isFine())
        { 
          log.fine("Resolved content '"+relativePath
            +"' in contentBaseURI  "+contentBaseURI
            );
        }
        return ret;
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
    URI relativeURI=URI.create(relativePath);
    return resolveCode(relativeURI);
  }
  
  /**
   * Given a path relative to this PathContext,
   *    find the associated code resource.
   * 
   * @param relativeURI
   * @return
   */  
  public Resource resolveCode(URI relativeURI)
    throws IOException
  {
    if (relativeURI.isAbsolute() 
        || ( relativeURI.getPath()!=null 
             && relativeURI.getPath().startsWith("/")
           )
      )
    { throw new IllegalArgumentException("URI must be relative "+relativeURI);
    }
    return resolveCodeSafe(relativeURI);
  }

  protected Resource resolveCodeSafe(URI relativeURI)
      throws IOException
  {
    Resource ret=null;

    URI effectiveCodeBaseURI=getEffectiveCodeBaseURI();
    if (effectiveCodeBaseURI!=null)
    {
      ret=Resolver.getInstance().resolve
        (getEffectiveCodeBaseURI().resolve(relativeURI));
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
    { ret=baseContext.resolveCodeSafe(relativeURI);
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
  
  public boolean checkGuard()
  { return guardX==null || Boolean.TRUE.equals(guardX.get());
  }
  
  @Override
  protected Focus<?> bindPeers(Focus<?> focus)
    throws ContextualException
  { 
    focus=super.bindPeers(focus);
    if (codeX!=null)
    { codeX.bind(focus);
    }
    if (guardX!=null)
    { guardX.bind(focus);
    }
    return focus;
  }

  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    if (constantImports!=null || imports!=null)
    { chain=chain.chain(chain.getSubject());
    }
    
    if (constantImports!=null)
    {
      for (Binding<?> binding: constantImports)
      { 
        binding.bind(chain);
        chain.addFacet(chain.chain(ConstantChannel.create(binding)));
      }
    }
    if (imports!=null)
    {
      for (Binding<?> binding: imports)
      { 
        binding.bind(chain);
        chain.addFacet(chain.chain(binding));
      }
    }

    if (baseContext!=null)
    { chain=baseContext.bind(chain);
    }
    
    chain=chain.chain(LangUtil.constantChannel(this));
    
    
    if (placeContext!=null)
    { chain(placeContext);
    }

    if (onEnter!=null)
    { Binding.bindArray(onEnter,chain);
    }
    if (onExit!=null)
    { Binding.bindArray(onExit,chain);
    }
    
    return chain;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> chain)
    throws ContextualException
  { 
    chain=super.bindExports(chain);
    if (constantExports!=null)
    { 
      for (Binding<?> binding:constantExports)
      { 
        binding.bind(chain);
        chain.addFacet(chain.chain(ConstantChannel.create(binding)));
      }
    }
    if (exports!=null)
    { 
      for (Binding<?> binding:exports)
      { 
        binding.bind(chain);
        chain.addFacet(chain.chain(binding));
      }
    }
    return chain;
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
  protected void pushLocal()
  {
    super.pushLocal();
    if (baseContext!=null)
    { baseContext.push();
    }
    if (onEnter!=null)
    { 
      for (Binding<?> binding:onEnter)
      { binding.get();
      }
    }
  }
  
  @Override
  protected void popLocal()
  {
    if (onExit!=null)
    { 
      for (Binding<?> binding:onExit)
      { binding.get();
      }
    }
    if (baseContext!=null)
    { baseContext.pop();
    }
    super.popLocal();
    
  }  
  @Override
  public void start()
    throws LifecycleException
  { 
    log.info("Starting "+this);
    if (baseContext!=null)
    { baseContext.start();
    }
    if (placeContext!=null)
    { placeContext.start();
    }
    log.info("Started "+this);
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    log.info("Stopping "+this);
    if (placeContext!=null)
    { placeContext.stop();
    }
    if (baseContext!=null)
    { baseContext.stop();
    }
    log.info("Stopped "+this);
  }

  @Override
  public String toString()
  { return super.toString()+": path="+absolutePath+", place="+placeContext+", base="+baseContext;
  }  
}
