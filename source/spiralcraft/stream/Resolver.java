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
package spiralcraft.stream;

import java.net.URI;

import java.util.HashMap;

import java.io.File;

import spiralcraft.stream.url.URLResourceFactory;
import spiralcraft.stream.classpath.ClasspathResourceFactory;
import spiralcraft.stream.file.FileResourceFactory;

/**
 * Resolves URIs into Resources
 */
public class Resolver
{
  private static Resolver _INSTANCE;

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
      }
      catch (AlreadyRegisteredException x)
      { 
        // Should never happen
        x.printStackTrace();
      }
    }
    return _INSTANCE;
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
    { uri=new File(System.getProperty("user.dir")).toURI().resolve(uri);
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
