//
//Copyright (c) 2010 Michael Toth
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
package spiralcraft.vfs.context;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandScheduler;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.task.Scenario;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.file.FileResource;
import spiralcraft.vfs.meta.Entry;

/**
 * <p>A Graft that synchronizes a local root with a remote. 
 * </p>
 * 
 * @author mike
 *
 */
public class Mirror
  implements Graft
{
  private static final ClassLog log=ClassLog.getInstance(Mirror.class);
  
  private Level debugLevel=ClassLog.getInitialDebugLevel(Mirror.class, null);
  
  private URI virtualURI;
  private URI localURI;
  private Binding<Long> lastModified;
  
  
  private final CommandScheduler updater
    =new CommandScheduler()
    {
      { 
        setPeriod(2000);
        setCommandFactory(new AbstractCommandFactory<Void,Void,Void>()
        {

          @Override
          public Command<Void, Void, Void> command()
          { 
            return new CommandAdapter<Void,Void,Void>()
            {
              @Override
              protected void run()
              { Mirror.this.triggerSubscriber();
              }
            };
          }
        }
        );
  
      }
    };
    
  private Scenario<?,?> subscriber;
  
  private URI remoteURI;
  
  @Override
  public URI getVirtualURI()
  { return virtualURI;
  }
  
  
  public void setVirtualURI(URI virtualURI)
  { this.virtualURI=virtualURI;
  }

  public URI getLocalURI()
  { return localURI;
  }
  
  
  public void setLocalURI(URI baseURI)
  { this.localURI=baseURI;
  }

  public void setRemoteURI(URI remoteURI)
  { this.remoteURI=remoteURI;
  } 
  
  public URI getRemoteURI()
  { return remoteURI;
  }
  
  public void setSubscriber(Scenario<?,?> subscriber)
  { this.subscriber=subscriber;
  }
  
  public void setLastModifiedX(Binding<Long> lastModified)
  { this.lastModified=lastModified;
  }
  
  public long computeLastModified()
  { return lastModified!=null?lastModified.get():0;
  }
  
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  
  public void triggerSubscriber()
  {
    if (subscriber!=null)
    {
      if (debugLevel.canLog(Level.FINE))
      { log.fine("Checking subscription...");
      }
      Command<?,?,?> command
        =subscriber.command();
      command.execute();
      if (command.getException()!=null)
      { 
        log.log
          (Level.WARNING
          ,"Subscriber for vfs mirror at '"+getVirtualURI()+"' threw exception"
          ,command.getException()
          );
      }
    }   
  }
  
  public void freshenEntry(Entry entry)
    throws IOException
  { 
    if (!entry.isContainer() && debugLevel.canLog(Level.TRACE) )
    { log.fine("Freshening "+entry.toString());
    }
    Resolver resolver=Resolver.getInstance();
    
    URI path=entry.getPath();
    if (path==null)
    { path=URIPool.create("");
    }
    boolean container=entry.isContainer();
    long lastModified=entry.getLastModified();
    
    if (path.isAbsolute() || path.getPath().startsWith("/"))
    { throw new IllegalArgumentException("Absolute path not accepted "+path);
    }
    
    if (container)
    {
      Resource localResource
        =resolver.resolve(localURI.resolve(path));
      localResource.ensureContainer();
      
    }
    else
    {
      URI absRemoteURI
        =remoteURI.isAbsolute()
        ?remoteURI
        :URIPool.get(URIPool.create("context:/").resolve(remoteURI))
        ;
      Resource remoteResource
        =resolver.resolve(absRemoteURI.resolve(path));
    
      File tempFile=File.createTempFile("spiralcraft.vfs.context.Mirror-",".part~");
      tempFile.deleteOnExit();
    
      try
      {
        Resource localResource
          =resolver.resolve(localURI.resolve(path));
        Resource tempResource
          =new FileResource(tempFile);
      
        remoteResource.copyTo(tempResource);
        
        
        if (localResource.exists())
        { localResource.delete();
        }
        tempResource.moveTo(localResource);
        localResource.setLastModified(lastModified);
      }
      finally
      { tempFile.delete();
      }
    }

  }
  

  
  @Override
  public Resource resolve(URI relativePath)
    throws UnresolvableURIException
  { return Resolver.getInstance().resolve(localURI.resolve(relativePath));
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    focusChain=focusChain.chain(LangUtil.constantChannel(this));
    if (subscriber!=null)
    { subscriber.bind(focusChain);
    }
    else
    { 
      log.warning
        ("Mirror for "+getLocalURI()+" has no configured subscriber and will"
        +" not be automatically synchronized"
        );
    }
    
    // XXX Change this to a metadata lookup for sync state.
    lastModified=new Binding<Long>(Expression.<Long>create(
      "[@:class:/spiralcraft/vfs/Resolver].@getInstance()"
      +" .resolve([:class:/spiralcraft/vfs/context/Mirror].localURI)"
      +" .[*:class:/spiralcraft/vfs/meta/TreeSnapshot].()"
      +" [!.container]"
      +" $[.lastModified.[*:class:/spiralcraft/lang/functions/Max]]"
    ));
    
    lastModified.bind(focusChain);
    
    return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  { 
    triggerSubscriber();
    updater.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { updater.stop();
  }

}
