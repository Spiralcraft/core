package spiralcraft.builder;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import spiralcraft.prefs.XmlPreferencesFactory;
import spiralcraft.prefs.XmlPreferencesNode;
import spiralcraft.prefs.ProxyPreferencesNode;
import spiralcraft.prefs.BackingStoreFormatException;

import java.net.URI;

/**
 * A PersistentReference stored in an XML 'preferences' format. 
 *
 * The preferences store contains a reference to the Assembly
 *   which defines the object and persistent state of the instance.
 *
 * The XmlObject can inherit from a 'base' object. This allows
 *   for the definition of prototype objects which can be 
 *   tailored by the end user or application for specific
 *   use cases. In this scenario, a reference to the base object
 *   will be stored in the preference store.
 *
 */
public class XmlObject
  extends PersistentReference
{
  private static final Preferences init(String store,String base,String assembly)
    throws BuildException
  {
    try
    { 
      
      URI storeUri=store!=null?URI.create(store):null;
      URI baseUri=base!=null?URI.create(base):null;
      
      Preferences prefs;
      if (storeUri!=null)
      { prefs=XmlPreferencesFactory.resourceRoot(storeUri);
      }
      else
      { prefs=new XmlPreferencesNode();
      }
      
      if (assembly!=null)
      { 
        if (!prefs.get("assemblyClass",assembly).equals(assembly))
        { throw new BuildException("Cannot change assembly class from "+prefs.get("assemblyClass",null));
        }
        else
        { prefs.put("assemblyClass",assembly);
        }
      }
      if (baseUri==null)
      { 
        base=prefs.get("base",null);
        if (base!=null)
        {
          baseUri=base!=null?URI.create(base):null;
          Preferences basePrefs=XmlPreferencesFactory.resourceRoot(baseUri);
          prefs.put("base",baseUri.toString());
          prefs=new ProxyPreferencesNode(prefs,basePrefs);
        }
      }
      else
      {
        Preferences basePrefs=XmlPreferencesFactory.resourceRoot(baseUri);
        prefs=new ProxyPreferencesNode(prefs,basePrefs);
      }
      return prefs;
    }
    catch (BackingStoreFormatException x)
    { throw new BuildException("Invalid object format in "+x.getURI()+": "+x.getMessage());
    }
    catch (BackingStoreException x)
    { throw new BuildException("Error reading persistence store",x);
    }
  }

  /**
   * Construct an XmlObject.
   *
   * If storeUri is non-null, the XmlObject will be loaded from it. 
   *
   * If baseUri is specified and the storeUri is new, the XmlObject will 'extend' the
   *   object read from the existing baseUri- ie. the baseUri will provide defaults for
   *   information not specified in the storeUri. The baseUri will be permanently registered
   *   within the store and cannot be changed.
   *
   * If assemblyUri is specified, and the storeUri is new, the XmlObject will be
   *   based on the specified assembly. An assemblyUri is required if the storeUri is new.
   */
  public XmlObject(String storeUri,String baseUri,String assemblyUri)
    throws BuildException
  { super(init(storeUri,baseUri,assemblyUri));
  }
}
