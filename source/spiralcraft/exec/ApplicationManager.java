package spiralcraft.exec;

import spiralcraft.registry.Registry;
import spiralcraft.registry.RegistryNode;

import spiralcraft.prefs.XmlPreferencesFactory;

import spiralcraft.loader.LibraryCatalog;

import spiralcraft.util.ArrayUtil;

import spiralcraft.builder.XmlObject;
import spiralcraft.builder.BuildException;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.UnresolvableURIException;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Controls the execution of application via one or more ApplicationEnvironments 
 */
public class ApplicationManager
{


  private static RegistryNode _REGISTRY_ROOT
    =Registry.getLocalRoot().createChild("applicationManager");

  private static ApplicationManager _INSTANCE=new ApplicationManager("boot");

  private LibraryCatalog _catalog=new LibraryCatalog();
  private final String _userId;
  private final RegistryNode _registryNode;

  private final URI _systemHomeEnvironmentURI
    =new File(System.getProperty("spiralcraft.home")).toURI().resolve("env/");

  private final URI _userHomeEnvironmentURI
    =new File(System.getProperty("user.home")).toURI().resolve(".spiralcraft/env/");

  private int _nextEnvironmentId=0;
  
  /**
   * Obtain the singleton instance of the ApplicationManager.
   */
  public static ApplicationManager getInstance()
  { return _INSTANCE;
  }

  public static void  shutdownInstance()
  { 
    _INSTANCE.shutdown();
    _INSTANCE=null;
  } 
  
  public ApplicationManager(String userId)
  { 
    _userId=userId;
    _registryNode=_REGISTRY_ROOT.createChild(_userId);
  }

  public void shutdown()
  { _catalog.close();
  }
  
  public LibraryCatalog getLibraryCatalog()
  { return _catalog;
  }

  public void exec(String[] args)
    throws ExecutionTargetException
  { 
    if (args.length==0)
    { 
      // Show environments in-scope
      throw new IllegalArgumentException("Please specify an application environment");
    }

    URI applicationURI=findEnvironment(args[0]);
    if (applicationURI==null)
    { 
      // Show environments in-scope
      throw new IllegalArgumentException("Unknown application environment '"+args[0]+"'");
    }
        
    args=(String[]) ArrayUtil.truncateBefore(args,1);

    try
    {

      XmlObject environmentRef=new XmlObject(applicationURI.toString(),null,null);
      environmentRef.register(_registryNode.createChild(Integer.toString(_nextEnvironmentId++)));
      
      ApplicationEnvironment environment=(ApplicationEnvironment) environmentRef.get();
      environment.setApplicationManager(this);
    
      environment.exec(args);
    }
    catch (BuildException x)
    { throw new ExecutionTargetException(x);
    }
  }

  private URI findEnvironment(String name)
  {
    URI nameURI=URI.create(name+".environment.xml");
    URI searchURI=null;
    
    
    if (nameURI.isAbsolute() && isEnvironment(nameURI))
    { return nameURI;
    }

    searchURI=_systemHomeEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    searchURI=_userHomeEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    searchURI=new File(System.getProperty("user.dir")).toURI().resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    return null;
  }

  private boolean isEnvironment(URI uri)
  {
    try
    { return Resolver.getInstance().resolve(uri).exists();
    }
    catch (UnresolvableURIException x)
    { System.err.println(x.toString());
    }
    catch (IOException x)
    { System.err.println(x.toString());
    }
    return false;
  }
}
