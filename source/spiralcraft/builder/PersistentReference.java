package spiralcraft.builder;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A reference to a persistent object- one with a lifetime
 *   longer than than that its in-vm references.
 *
 * A PersistentReference is an assocation of an AssemblyClass with
 *   a Preferences subtree. The root of the Preferences subtree identifies
 *   the specific AssemblyClass, and the properties of the object and its children
 *   are stored in the descendants.
 *
 * When the PersistentReference is instantiated, the AssemblyClass is resolved,
 *   an in-memory copy is instantiated within an optional parent assembly,
 *   and specific properties are set from the Preferences subtree. 
 *
 * When the PersistentReference is flushed, the properties are saved into the
 *   Preferences subtree.
 */
public class PersistentReference
  implements Registrant
{
  private final Preferences _preferences;
  private final Assembly _assembly;
  private final String _assemblyClassURI;
  
  public PersistentReference(Preferences prefs,Assembly parent)
    throws BuildException
  {
    _preferences=prefs;
    
    _assemblyClassURI=prefs.get("assemblyClass",null);
    if (_assemblyClassURI==null)
    { throw new PersistenceException("No assemblyClass specified in object definition");
    }

    try
    { 
      URI uri=new URI(_assemblyClassURI+".assembly.xml");
      AssemblyClass assemblyClass=AssemblyLoader.getInstance().findAssemblyDefinition(uri);
      if (assemblyClass==null)
      { throw new PersistenceException("AssemblyClass definition '"+uri+"' not found");
      }
      _assembly=assemblyClass.newInstance(parent);
      
    }
    catch (URISyntaxException x)
    { 
      throw new PersistenceException
        ("Bad assemblyClass name '"+_assemblyClassURI+"'",x);
    }
    
  }
  
  public void register(RegistryNode node)
  {
    node.registerInstance(Preferences.class,_preferences);
    _assembly.register(node.createChild("instance"));
  }
  
  public Object get()
  { return _assembly.getSubject().get();
  }
  
  public void flush()
    throws PersistenceException
  { 
    _assembly.savePreferences();
    try
    { _preferences.flush();
    }
    catch (BackingStoreException x)
    { throw new PersistenceException("Error saving object in "+_assemblyClassURI,x);
    }
  }
  
}
