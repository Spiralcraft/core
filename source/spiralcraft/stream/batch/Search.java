//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.stream.batch;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

import spiralcraft.util.Arguments;

import spiralcraft.stream.filters.ContentRegexFilter;
import spiralcraft.stream.filters.NameGlobFilter;
import spiralcraft.stream.filters.ListFilter;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;
import spiralcraft.stream.Container;
import spiralcraft.stream.ResourceFilter;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Stack;
import java.util.List;

import java.io.IOException;
import java.io.File;

import java.util.regex.PatternSyntaxException;


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
  private Operation _operation;
  private Operation _currentOperation;

  /**
   * Execute a search specified by arguments
   */
  public void execute(ExecutionContext context,String[] args)
  { 
    configure(args);
    if (getRootURI()==null)
    { setRootURI(context.focusURI());
    }
    run();
  }

  /**
   * Configure the search from the specific arguments
   */
  public void configure(String[] args)
  {
    new Arguments()
    { 
      protected boolean processOption(String option)
      { 
        if (_currentOperation!=null 
            && _currentOperation.processOption(this,option)
           )
        { return true;
        }
        else if (option=="name")
        { setNameGlob(nextArgument());
        }
        else if (option=="contains")
        { setContains(nextArgument());
        }
        else if (option=="prepend")
        { addOperation(new PrependOperation());
        }
        else if (option=="print")
        { addOperation(new PrintOperation());
        }
        else
        { return false;
        }
        return true;
      }

      protected boolean processArgument(String argument)
      { 
        if (_currentOperation!=null 
            && _currentOperation.processArgument(this,argument)
           )
        { return true;
        }
        setRoot(argument);
        return true;
      }
      
    }
    .process(args,'-');
  }
  
  /**
   * Run an already configured search
   */
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
                    if (_operation!=null)
                    { _operation.invoke(child);
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
    catch (OperationException x)
    { x.printStackTrace();
    }
  }

  /**
   * Run a configured search and return a List of the results
   */
  public synchronized List list()
  { 
    ListFilter filter=new ListFilter();
    setFilter(filter);
    run();
    return filter.getList();
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
  
  public void addOperation(Operation op)
  { 
    if (_currentOperation!=null)
    { _currentOperation.setNextOperation(op);
    }
    _currentOperation=op;
    if (_operation==null)
    { _operation=op;
    }
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
