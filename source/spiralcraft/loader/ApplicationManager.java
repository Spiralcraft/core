package spiralcraft.loader;

import spiralcraft.registry.Registry;
import spiralcraft.registry.RegistryNode;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import spiralcraft.prefs.XmlPreferencesFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Controls the loading of applications for a user environment
 */
public class ApplicationManager
{

  private static ApplicationManager _INSTANCE;

  private static RegistryNode _REGISTRY_ROOT
    =Registry.getLocalRoot().createChild("applicationManager");

  private static Aliases _DEFAULT_ALIASES=new Aliases();

  private LibraryCatalog _catalog=new LibraryCatalog();
  private final String _userId;
  private final RegistryNode _registryNode;

  /**
   * Obtain the singleton instance of the ApplicationManager.
   *
   * Creates the instance if it does not exist already.
   */
  public static synchronized ApplicationManager getInstance()
  { 

    if (_INSTANCE==null)
    { 
      try
      {
        _REGISTRY_ROOT.registerInstance
          (Preferences.class
          ,XmlPreferencesFactory.resourceRoot
            (new URI
              ("file:///"
              +System.getProperty("spiralcraft.home").replace('\\','/')
              +"/config/applicationManager.preferences.xml"
              )
            )
          );

        _DEFAULT_ALIASES.register(_REGISTRY_ROOT.createChild("aliases"));
      }
      catch (BackingStoreException x)
      { x.printStackTrace();
      }
      catch (URISyntaxException x)
      { x.printStackTrace();
      }

      _INSTANCE=new ApplicationManager("default");
    }
    return _INSTANCE;
  }

  public ApplicationManager(String userId)
  { 
    _userId=userId;
    _registryNode=_REGISTRY_ROOT.createChild(_userId);
  }

  /**
   * Create a default application environment.
   */
  public ApplicationEnvironment createApplicationEnvironment()
  { return new ApplicationEnvironment(this);
  }

  public LibraryCatalog getLibraryCatalog()
  { return _catalog;
  }

  public Aliases getAliases()
  { return _DEFAULT_ALIASES;
  }
}
