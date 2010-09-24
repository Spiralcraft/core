package spiralcraft.vfs.task;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
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
          if (verbose)
          {
            log.info
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
    throws BindException
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
          
          ExcludesFilter excludesFilter=null;
          if (patterns!=null)
          { excludesFilter=new ExcludesFilter(sourceResource.getURI(),patterns);
          } 
        
          Search search=new Search();
          if (excludesFilter!=null)
          {
            search.setExpansionFilter(excludesFilter);
            search.setFilter(excludesFilter);
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
              
            if (verbose)
            {
              log.info
                ("Copying "+from.getURI()+" to "+to.getURI());
            }
            
            if (from.asContainer()!=null)
            { 
              to.ensureContainer();
              if (preserveTime)
              { to.setLastModified(from.getLastModified());
              }
            }
            else if (!overwrite && to.exists())
            { 
            }
            else
            { 
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
