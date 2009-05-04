package spiralcraft.vfs.task;

import java.io.IOException;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.util.PathPattern;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.batch.Search;

public class Copy
  extends Scenario<Task,Void>
{

  private Resource sourceResource;
  private Resource targetResource;
  private boolean useDefaultExcludes=true;
  private PathPattern pattern;
  
  private ResourceFilter defaultExcludes
    =new ResourceFilter()
  {
    @Override
    public boolean accept(Resource resource)
    {
      if (!useDefaultExcludes)
      { return true;
      }
      String localName=resource.getLocalName();
      return localName==null 
        || localName.equals("") 
        || !(localName.equals("CVS")
             || localName.equals(".svn")
            );
      
    }
  };
  
  @Override
  protected Task task()
  { return new CopyTask();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  { return super.bind(focusChain);
  }
  
  public void setSourceResource(Resource sourceResource)
  { this.sourceResource=sourceResource;
    
  }
  
  public void setTargetResource(Resource targetResource)
  { this.targetResource=targetResource;
  }

  public void setPattern(PathPattern pattern)
  { this.pattern=pattern;
  }
  
  protected class CopyTask
    extends AbstractTask<Void>
  {
    @Override
    public void work()
    {  
      try
      {
        Container sourceContainer=sourceResource.asContainer();
        if (sourceContainer==null)
        {
          if (debug)
          { log.debug(sourceResource.getURI()+" is not a container");
          }
          // Source is a file
          sourceResource.copyTo(targetResource);
        }
        else
        {
          if (targetResource.asContainer()==null)
          { 
            throw new IOException
              ("Target must be a directory or other container");
          }
          
          // Source is a dir
          Search search=new Search();
          search.setFilter(defaultExcludes);
          search.setRootResource(sourceResource);
          if (pattern!=null)
          { search.setPattern(pattern);
          }
          List<Resource> result=search.list();
          for (Resource resource:result)
          { 
            if (debug)
            { 
              log.debug
                (sourceResource.getURI()
                  .relativize(resource.getURI()).toString()
                );
//              log.debug(resource.toString());
            }
            if (verbose)
            {
              log.info
                ("Copying "+resource.getURI()+" to "+targetResource.getURI());
            }
            resource.copyTo(targetResource);
          }
          
        }
      }
      catch (IOException x)
      { addException(x);
      }
    }
  }

}
