//
// Copyright (c) 2011 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.vfs.task;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Path;
import spiralcraft.util.PathPattern;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.batch.Search;
import spiralcraft.vfs.filters.PatternFilter;


/**
 * Copy one or more VFS resources
 */
public class Copy
  extends Scenario<Void,Void>
{

  private Resource sourceResource;
  private Binding<URI> sourceUriX;
  private Resource targetResource;
  private Binding<URI> targetUriX;
  private boolean useDefaultExcludes=true;
  private PathPattern pattern;
  private boolean overwrite;
//  private boolean testRun;
  private boolean preserveTime;
  private String[] excludes;
  private Binding<String[]> excludesX;
  private boolean excludeUnchanged;
  
  
  class ExcludesFilter
    implements ResourceFilter
  {
    private PatternFilter[] patterns;
    
    public ExcludesFilter(URI prefix,String[] excludes)
    {
      patterns=new PatternFilter[excludes.length];
      int i=0;
      for (String pattern:excludes)
      { patterns[i++]=new PatternFilter
          (new PathPattern(new Path(prefix.getPath(),'/'),pattern)
          );
      }
    }
    

    
    @Override
    public boolean accept(Resource resource)
    {

      String localName=resource.getLocalName();
//      log.fine("Checking filter for "+localName);
      if (localName==null 
          || localName.equals("") 
          )
      { return true;
      }
      
      for (PatternFilter pattern:patterns)
      { 
        if (pattern.accept(resource))
        { 
          if (debug)
          {
            log.fine
              ("Excluding "+resource.getURI());
          }
          return false;
        }
        else
        { 
//          log.fine("NO MATCH "+pattern.getPattern().toString()
//            +": "+resource.getURI());
        }
      }
      return true;
      
    }
  };
  
  @Override
  protected Task task()
  { return new CopyTask();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  { 
    if (sourceUriX!=null)
    { sourceUriX.bind(focusChain);
    }
    if (targetUriX!=null)
    { targetUriX.bind(focusChain);
    }
    if (excludesX!=null)
    { excludesX.bind(focusChain);
    }
    return super.bind(focusChain);
  }
  
  public void setSourceUriX(Binding<URI> sourceUriX)
  { this.sourceUriX=sourceUriX;
  }
  
  public void setSourceResource(Resource sourceResource)
  { 
    this.sourceResource=sourceResource;
  }
  
  public void setTargetUriX(Binding<URI> targetUriX)
  { this.targetUriX=targetUriX;
  }

  public void setTargetResource(Resource targetResource)
  { this.targetResource=targetResource;
  }

  public void setExcludesX(Binding<String[]> excludesX)
  { this.excludesX=excludesX;
  }

  public void setExcludeUnchanged(boolean excludeUnchanged)
  { this.excludeUnchanged=excludeUnchanged;
  }
  
  public void setPattern(PathPattern pattern)
  { this.pattern=pattern;
  }
  
  public void setPreserveTime(boolean preserveTime)
  { this.preserveTime=preserveTime;
  }
  
  public void setOverwrite(boolean overwrite)
  { this.overwrite=overwrite;
  }
  
  private Resource resolveWithDefault(Binding<URI> binding,Resource rdefault)
    throws UnresolvableURIException
  {
    Resource resource=null;
    if (binding!=null)
    { 
      URI uri=binding.get();
      if (uri!=null)
      { resource=Resolver.getInstance().resolve(uri);
      }
    }
    if (resource==null)
    { resource=rdefault;
    }
    return resource;
  }
  
  
  protected class CopyTask
    extends AbstractTask
  {
    @Override
    public void work()
      throws InterruptedException
    {  
      try
      {
        PrintStream info=verbose?ExecutionContext.getInstance().out():null;
        
        Resource sourceResource
          =resolveWithDefault(sourceUriX,Copy.this.sourceResource);
        
        Resource targetResource
          =resolveWithDefault(targetUriX,Copy.this.targetResource);

        Container sourceContainer=sourceResource.asContainer();
        if (sourceContainer==null)
        {
          // Copy single file
          
          if (debug)
          { log.debug(sourceResource.getURI()+" is not a container");
          }
          // Source is a file
          sourceResource.copyTo(targetResource);
        }
        else
        {
          // Copy a tree
          
          if (targetResource.asContainer()==null)
          { 
            throw new IOException
              ("Target must be a directory or other container");
          }
          
          Resource targetRoot=targetResource;
          if (pattern==null)
          { 
            URI targetURI=targetResource.getURI();
            if (targetURI.getPath()!=null)
            { targetURI=URIUtil.addPathSuffix(targetURI,"/");
            }
            
            // source indicates directory itself, not contents
            targetRoot
              =Resolver.getInstance().resolve
                (targetURI.resolve
                    (sourceResource.getLocalName()+"/")
                );
            if (!targetRoot.exists())
            { targetRoot.ensureContainer();
            }
          }
          
          String[] patterns=null;
          if (excludesX!=null)
          { patterns=excludesX.get();
          }
          if (patterns==null && Copy.this.excludes!=null)
          { patterns=Copy.this.excludes;
          }
          if (patterns==null && Copy.this.useDefaultExcludes)
          { patterns=new String[] {"**/CVS/**","**/.svn/**"};
          }
          
          if (debug)
          { 
            if (patterns!=null)
            { log.fine("Excluding: "+ArrayUtil.format(patterns,",","\""));
            }
            else
            { log.fine("No excludes");
            }
          }
          
          ResourceFilter excludesFilter=null;
          if (patterns!=null)
          { 
            excludesFilter=PatternFilter.any
              (new Path(sourceResource.getURI().getPath(),'/'),patterns);
          } 
        
          Search search=new Search();
          if (excludesFilter!=null)
          { search.setExclusionFilter(excludesFilter);
          }
          search.setRootResource(sourceResource);
          if (pattern!=null)
          { search.setPattern(pattern);
          }
          List<Resource> result=search.list();
          for (Resource from:result)
          { 
            URI relativeURI=sourceResource.getURI()
              .relativize(from.getURI());
            if (debug)
            { 
              log.debug
                (relativeURI.toString()
                );
//              log.debug(resource.toString());
            }
            
            Resource to=Resolver.getInstance().resolve
              (targetRoot.getURI().resolve(relativeURI));
              
            
            if (from.asContainer()!=null)
            { 
              if (verbose && !to.exists())
              {
                info.println("Creating container "+to.getURI());
              }
              to.ensureContainer();
              if (preserveTime)
              { to.setLastModified(from.getLastModified());
              }
            }
            else if (!overwrite && to.exists())
            { 
            }
            else if (excludeUnchanged 
                    && from.getLastModified()==to.getLastModified()
                    && from.getSize()==to.getSize()
                    )
            {
            }
            else
            { 
              if (verbose)
              {
                info.println("Copying "+from.getURI()+" to "+to.getURI());
              }
              
              from.copyTo(to);
              if (preserveTime)
              { to.setLastModified(from.getLastModified());
              }
              
            }

          }
          
        }
      }
      catch (IOException x)
      { addException(x);
      }
    }
  }

}
