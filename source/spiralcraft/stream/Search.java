package spiralcraft.stream;

import spiralcraft.exec.Executable;

import spiralcraft.util.Arguments;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Stack;

import java.io.IOException;

import spiralcraft.stream.filters.ContentRegexFilter;
import spiralcraft.stream.filters.NameGlobFilter;

import java.util.regex.PatternSyntaxException;

import java.io.File;

/**
 * Regexp Search tool for StreamResources
 */
public class Search
  implements Executable,Runnable
{
  private URI _rootUri;
  private ResourceFilter _filter;
  private ResourceFilter _nameFilter;
  private ResourceFilter _contentFilter;
  private boolean _print;


  public void execute(String[] args)
  { 
    new Arguments()
    { 
      protected boolean processOption(String option)
      { 
        if (option=="name")
        { setNameGlob(nextArgument());
        }
        else if (option=="contains")
        { setContains(nextArgument());
        }
        else
        { return false;
        }
        return true;
      }

      protected boolean processArgument(String argument)
      { 
        setRoot(argument);
        return true;
      }
      
    }
    .process(args,'-');

    setPrint(true);
    if (getRootURI()==null)
    { 
      // XXX Deal properly with environment, requires Executable API change
      setRootURI(new File(new File(".").getAbsolutePath()).toURI());
    }
    else if (!getRootURI().isAbsolute())
    { setRootURI(new File(new File(".").getAbsolutePath()).toURI().resolve(getRootURI()));
    }
    run();
  }

  public synchronized void run()
  { 
    final Stack stack=new Stack();

    try
    { stack.push(Resolver.getInstance().resolve(_rootUri));
    }
    catch (UnresolvableURIException x)
    { 
      x.printStackTrace();
      return;
    }

    try
    {

      while (!stack.isEmpty())
      {
        Resource resource=(Resource) stack.pop();
        Container container=resource.asContainer();
        if (container!=null)
        { 
          Resource[] children=container.listChildren();
          if (children!=null)
          {
            for (int i=children.length;i-->0;)
            { 
              Resource child=children[i];
              if (child.asContainer()!=null)
              { stack.push(child);
              }
              
              if (_nameFilter==null || _nameFilter.accept(child))
              {
                if (_contentFilter==null || _contentFilter.accept(child))
                {
                  if (_filter==null || _filter.accept(child))
                  { 
                    if (_print)
                    { System.out.println(child.getURI().toString());
                    }
                  }
                }
              }

            }
          }
        }
      }
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    catch (PatternSyntaxException x)
    { x.printStackTrace();
    }
  }

  public void setRootURI(URI root)
  { _rootUri=root;
  }

  public URI getRootURI()
  { return _rootUri;
  }

  public void setNameGlob(String name)
  { _nameFilter=new NameGlobFilter(name);
  }

  public void setContains(String contains)
  { _contentFilter=new ContentRegexFilter(contains);
  }

  /**
   * Indicate that matching resource URIs should be printed to 
   *   the console.
   */
  public void setPrint(boolean print)
  { _print=print;
  }

  public void setFilter(ResourceFilter filter)
  { _filter=filter;
  }

  public void setRoot(String root)
  { 
    try
    { setRootURI(new URI(root));
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(root);
    }
  }


  
}
