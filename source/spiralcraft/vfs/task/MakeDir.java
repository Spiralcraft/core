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
import spiralcraft.vfs.UnresolvableURIException;


/**
 * Copy one or more VFS resources
 */
public class MakeDir
  extends Scenario<Void,Void>
{

  private Binding<URI> dirX;
  private Resource dir;
  
  public void setDirX(Binding<URI> dirX)
  { this.dirX=dirX;
  }
  
  public void setDir(URI dir) throws UnresolvableURIException
  { this.dir=Resolver.getInstance().resolve(dir);
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
  { return new MakeDirTask();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  { 
    if (dirX!=null)
    { dirX.bind(focusChain);
    }
    return super.bind(focusChain);
  }
  
  
  protected class MakeDirTask
    extends AbstractTask
  {
    @Override
    public void work()
      throws InterruptedException
    {  
      try
      {
        Resource dir
          =resolveWithDefault(dirX,MakeDir.this.dir);
        
        dir.ensureContainer();
      }
      catch (IOException x)
      { addException(x);
      }
    }
  }

}
