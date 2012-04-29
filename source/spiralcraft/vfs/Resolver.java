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
import java.util.Stack;

import java.io.File;

import spiralcraft.exec.ExecutionContext;
import spiralcraft.util.string.StringConverter;
import spiralcraft.vfs.classpath.ClasspathResourceFactory;
import spiralcraft.vfs.context.ContextResourceFactory;
import spiralcraft.vfs.file.FileResourceFactory;
import spiralcraft.vfs.url.URLResourceFactory;
import spiralcraft.vfs.util.NullResource;
import spiralcraft.vfs.jar.JarResourceFactory;
import spiralcraft.vfs.ovl.OverlayResourceFactory;

/**
 * <P>Resolves URIs into Resources. Each supported URI scheme is mapped to a 
 *   ResourceFactory.
 *   
 * <P>When a relative URI is encountered, it is resolved against a contextual
 *   URI currently in effect for the executing Thread. Components that wish
 *   to change the contextual URI for subcomponents they will be calling into
 *   should use the following mechanism:
 * 
 * <P><CODE><PRE>
 *   URI localURI=<I>someLocalURI</I>;
 *   Resolver.pushThreadContextURI(localURI);
 *   try
 *   { doSomeWork();
 *   }
 *   finally
 *   { resolver.popThreadContextURI();
 *   }
 *   </PRE></CODE>
 */
public class Resolver
{
  private static Resolver _INSTANCE;
  
  private static final ThreadLocal<Stack<URI>> contextURI
    =new ThreadLocal<Stack<URI>>()
  {
    @Override
    protected Stack<URI> initialValue()
    { 
      Stack<URI> stack=new Stack<URI>();
      stack.push(new File(System.getProperty("user.dir")).toURI());
      return stack;
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
        ClasspathResourceFactory classFactory=new ClasspathResourceFactory();
        _INSTANCE.registerResourceFactory
          ("java",classFactory);
        _INSTANCE.registerResourceFactory
          ("class",classFactory);
        _INSTANCE.registerResourceFactory
          ("file",new FileResourceFactory());
        _INSTANCE.registerResourceFactory
          ("context",new ContextResourceFactory());
        _INSTANCE.registerResourceFactory
          ("jar",new JarResourceFactory());
        _INSTANCE.registerResourceFactory
          ("ovl",new OverlayResourceFactory());
        _INSTANCE.registerResourceFactory
          ("null",new NullResource.Factory());
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
  { return contextURI.get().peek();
  }

  public static final void pushThreadContextURI(URI uri)
  { contextURI.get().push(uri);
  }
  
  public static final void popThreadContextURI()
  { contextURI.get().pop();
  }
  
  static { 
    StringConverter.registerInstance
      (Resource.class
      ,new StringConverter<Resource>()
      {
        @Override
        public Resource fromString(String val)
        { 
          try
          {
            return Resolver.getInstance()
              .resolve(ExecutionContext.getInstance()
                 .canonicalize(URI.create(val))
                 );
          }
          catch (Exception x)
          { throw new IllegalArgumentException(x);
          }
        
        }
        
        @Override
        public String toString(Resource resource)
        { return resource.getURI().toString();
        }
      }
      );
      
  }
  
  /**
   * Specify the default ResourceFactory to use for unknown schemes
   */
  public void setDefaultFactory(ResourceFactory factory)
  { _defaultFactory=factory;
  }

  /**
   * <p> Obtain a Resource that provides access to the content identified by
   *    the URI. If the URI is relative, it will be resolved against the
   *    user directory (Java VM working directory).
   * </p>
   * 
   * 
   * @return A Resource with provides access to the content identified by 
   *   the URI
   */
  public Resource resolve(String uri)
    throws UnresolvableURIException
  { return resolve(URI.create(uri));
  }

  /**
   * <p> Obtain a Resource that provides access to the content identified by
   *    the URI. If the URI is relative, it will be resolved against the
   *    user directory (Java VM working directory).
   * </p>
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
  
  /**
   * Whether the specified URI scheme points to a resource
   * 
   * @param scheme
   * @return
   */
  public boolean handlesScheme(String scheme)
  {
    if (scheme.equals("dynamic"))
    { return false;
    }
    if (scheme.equals("urn"))
    { return false;
    }
    if (_resourceFactories.get(scheme)!=null)
    { return true;
    }
    if (_defaultFactory!=null)
    { 
      if (_defaultFactory.handlesScheme(scheme))
      { return true;
      }
      else if (this!=getInstance())
      { return getInstance().handlesScheme(scheme);
      }
    }
    return false;
  }
  
  /**
   * 
   * @param uri
   * @return The absolute, globally resolvable URI that corresponds to the 
   *  resource referenced by the specified URI.
   * @throws UnresolvableURIException
   */
  public URI canonicalize(URI uri)
    throws UnresolvableURIException
  { 
    Resource resource=resolve(uri);
    return resource.getURI();
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
