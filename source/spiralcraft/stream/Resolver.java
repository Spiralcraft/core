package spiralcraft.stream;

import java.net.URI;

import java.util.HashMap;

import spiralcraft.stream.url.URLResourceFactory;
import spiralcraft.stream.classpath.ClasspathResourceFactory;
import spiralcraft.stream.file.FileResourceFactory;

/**
 * Resolves URIs into Resources
 */
public class Resolver
{
  private static Resolver _INSTANCE;

  private HashMap _resourceFactories=new HashMap();
  private ResourceFactory _defaultFactory;

  public static synchronized Resolver getInstance()
  {
    if (_INSTANCE==null)
    { 
      _INSTANCE=new Resolver();
      _INSTANCE.setDefaultFactory(new URLResourceFactory());
      try
      {
        _INSTANCE.registerResourceFactory("java",new ClasspathResourceFactory());
        _INSTANCE.registerResourceFactory("file",new FileResourceFactory());
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

  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { 
    ResourceFactory factory=null;
    synchronized (_resourceFactories)
    { 
      factory
        =(ResourceFactory) _resourceFactories.get(uri.getScheme());
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
