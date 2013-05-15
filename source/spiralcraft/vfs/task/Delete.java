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
import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceUtil;
import spiralcraft.vfs.UnresolvableURIException;


/**
 * Delete a VFS resources
 */
public class Delete
  extends Scenario<Void,Void>
{

  private Binding<URI> fileX;
  private Resource file;
  private boolean recursive;
  
  public void setFileX(Binding<URI> fileX)
  { this.fileX=fileX;
  }
  
  public void setFile(URI file) throws UnresolvableURIException
  { this.file=Resolver.getInstance().resolve(file);
  }
  
  /**
   * Recursively delete a container and all of its children
   * 
   * @param recursive
   */
  public void setRecursive(boolean recursive)
  { this.recursive=recursive;
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
  
  @Override
  protected Task task()
  { return new DeleteTask();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  { 
    if (fileX!=null)
    { fileX.bind(focusChain);
    }
    return super.bind(focusChain);
  }
  
  
  protected class DeleteTask
    extends AbstractTask
  {
    @Override
    public void work()
      throws InterruptedException
    {  
      try
      {
        Resource file
          =resolveWithDefault(fileX,Delete.this.file);
        if (file.exists())
        { 
          if (recursive)
          { ResourceUtil.deleteRecursive(file);
          }
          else
          { file.delete();
          }
          if (file.exists())
          { throw new IOException("Could not delete "+file);
          }
        }
      }
      catch (IOException x)
      { addException(x);
      }
    }
  }

}
