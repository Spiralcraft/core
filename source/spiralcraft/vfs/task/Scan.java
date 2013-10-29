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
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.Chain;
import spiralcraft.task.Task;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Path;
import spiralcraft.util.PathPattern;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.batch.Search;
import spiralcraft.vfs.filters.PatternFilter;


/**
 * Scan a set of VFS resources and perform a subtask in the context of each
 */
public class Scan
  extends Chain<Void,Void>
{

  private Resource sourceResource;
  private Binding<URI> sourceUriX;
  private boolean useDefaultExcludes=true;
  private PathPattern pattern;
//  private boolean testRun;
  private String[] excludes;
  private Binding<String[]> excludesX;
  private ThreadLocalChannel<Resource> resourceChannel;
  
  
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
  { return new ScanTask();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  { 
    if (sourceUriX!=null)
    { sourceUriX.bind(focusChain);
    }
    if (excludesX!=null)
    { excludesX.bind(focusChain);
    }
    return super.bind(focusChain);
  }
  
  @Override
  public Focus<?> bindExports(Focus<?> focusChain)
  { 
    resourceChannel=new ThreadLocalChannel<Resource>
      (BeanReflector.<Resource>getInstance(Resource.class));
    return focusChain.chain(resourceChannel);
  }
  
  public void setSourceUriX(Binding<URI> sourceUriX)
  { 
    sourceUriX.setTargetType(URI.class);
    this.sourceUriX=sourceUriX;
  }
  
  public void setRootResource(Resource sourceResource)
  { 
    this.sourceResource=sourceResource;
  }
  
  public void setRootURI(URI root)
    throws UnresolvableURIException
  { 
    this.sourceResource=Resolver.getInstance().resolve(root);
  }
  

  public void setExcludesX(Binding<String[]> excludesX)
  { this.excludesX=excludesX;
  }
  
  public void setPattern(PathPattern pattern)
  { this.pattern=pattern;
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
  
  
  protected class ScanTask
    extends ChainTask
  {
    @Override
    public void work()
      throws InterruptedException
    {  
      try
      {
        PrintStream info=verbose?ExecutionContext.getInstance().out():null;
        
        Resource sourceResource
          =resolveWithDefault(sourceUriX,Scan.this.sourceResource);
        
        Container sourceContainer=sourceResource.asContainer();
        if (sourceContainer==null)
        {
          // Copy single file
          
          if (debug)
          { log.debug(sourceResource.getURI()+" is not a container");
          }

          // Source is a file
          if (info!=null)
          { info.println(sourceResource.getURI());
          }
          resourceChannel.push(sourceResource);
          try
          { super.work();
          }
          finally
          { resourceChannel.pop();
          }     
        }
        else
        {
          // Copy a tree

                   
          String[] patterns=null;
          if (excludesX!=null)
          { patterns=excludesX.get();
          }
          if (patterns==null && Scan.this.excludes!=null)
          { patterns=Scan.this.excludes;
          }
          if (patterns==null && Scan.this.useDefaultExcludes)
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
            
            if (info!=null)
            { info.println(from.getURI());
            }
            resourceChannel.push(from);
            try
            { super.work();
            }
            finally
            { resourceChannel.pop();
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
