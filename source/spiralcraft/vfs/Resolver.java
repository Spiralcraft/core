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
package spiralcraft.vfs;

import java.net.URI;

import java.util.HashMap;

import java.io.File;

import spiralcraft.vfs.classpath.ClasspathResourceFactory;
import spiralcraft.vfs.context.ContextResourceFactory;
import spiralcraft.vfs.file.FileResourceFactory;
import spiralcraft.vfs.url.URLResourceFactory;

/**
 * Resolves URIs into Resources
 */
public class Resolver
{
  private static Resolver _INSTANCE;
  
  private static final ThreadLocal<URI> contextMap
    =new ThreadLocal<URI>()
  {
    protected URI initialValue()
    { return new File(System.getProperty("user.dir")).toURI();
    }
  };

  private HashMap<String,ResourceFactory> _resourceFactories
    =new HashMap<String,ResourceFactory>();
  
  private ResourceFactory _defaultFactory;
  
  public static synchronized Resolver getInstance()
  {
    if (_INSTANCE==null)
    { 
      _INSTANCE=new Resolver();
      _INSTANCE.setDefaultFactory(new URLResourceFactory());
      try
      {
        // Consider auto-registering "class","classpath","class-resource",
        //   or moving this mapping to a config mechanism
        _INSTANCE.registerResourceFactory
          ("java",new ClasspathResourceFactory());
        _INSTANCE.registerResourceFactory
          ("file",new FileResourceFactory());
        _INSTANCE.registerResourceFactory
          ("context",new ContextResourceFactory());
      }
      catch (AlreadyRegisteredException x)
      { 
        // Should never happen
        x.printStackTrace();
      }
    }
    return _INSTANCE;
  }

  

  public static final URI getThreadContextURI()
  { return contextMap.get();
  }

  public static final void setThreadContextURI(String name,URI uri)
  { contextMap.set(uri);
  }
  
  /**
   * Specify the default ResourceFactory to use for unknown schemes
   */
  public void setDefaultFactory(ResourceFactory factory)
  { _defaultFactory=factory;
  }

  /**
   * <P> Obtain a Resource that provides access to the content identified by
   *    the URI. If the URI is relative, it will be resolved against the
   *    user directory (Java VM working directory).
   * 
   * 
   * @return A Resource with provides access to the content identified by 
   *   the URI
   */
  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { 
    if (!uri.isAbsolute())
    { uri=getThreadContextURI().resolve(uri);
    }
    
    ResourceFactory factory=null;
    synchronized (_resourceFactories)
    { 
      factory
        =_resourceFactories.get(uri.getScheme());
    }
      
    if (factory==null)
    { factory=_defaultFactory;
    }

    if (factory==null)
    { 
      if (this!=getInstance())
      { return getInstance().resolve(uri);
      }
      else
      { throw new UnresolvableURIException(uri,"Unknown scheme");
      }
    }
    
    return factory.resolve(uri);
    
  }

  public void registerResourceFactory
    (String scheme
    ,ResourceFactory factory
    )
    throws AlreadyRegisteredException
  { 
    synchronized (_resourceFactories)
    {
      if (_resourceFactories.get(scheme)!=null)
      { throw new AlreadyRegisteredException(scheme);
      }
      _resourceFactories.put(scheme,factory);
    }
  }

}
