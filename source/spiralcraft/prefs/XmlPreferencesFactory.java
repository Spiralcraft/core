package spiralcraft.prefs;

import java.util.prefs.PreferencesFactory;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
 
import java.io.File;

public class XmlPreferencesFactory
  implements PreferencesFactory
{
  private static HashMap _cache=new HashMap();

  private Preferences _systemRoot;
  private Preferences _userRoot;

  public Preferences systemRoot()
  { return _systemRoot;
  }

  public Preferences userRoot()
  { return _userRoot;
  }

  /**
   * Returns a Preferences root node from an XML resource in the user profile
   *   named path+".preferences.xml"
   */
  public static Preferences userProfileResourceRoot(String path)
    throws BackingStoreException
  {

    // XXX Need to use some standard API for determining user preferences resource
    File spiralcraftDir
      =new File(System.getProperty("user.home"),".spiralcraft");
    File prefsDir=new File(spiralcraftDir,"preferences");
    if (!prefsDir.exists())
    { prefsDir.mkdirs();
    }
    
    File prefsFile=new File(prefsDir,path+".preferences.xml");
    return resourceRoot(prefsFile.toURI());
    
  }

  /**
   * Obtain a Preferences interface backed by the specified resource URI
   */
  public static Preferences resourceRoot(URI resourceUri)
    throws BackingStoreException
  { 
    synchronized (_cache)
    {
      Preferences prefs=(Preferences) _cache.get(resourceUri);
      if (prefs==null)
      { 
        prefs=new XmlPreferencesNode(resourceUri);
        _cache.put(resourceUri,prefs);
      }
      return prefs;
    }
  }
}
