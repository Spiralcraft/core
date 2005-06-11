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

  public static Preferences resourceRoot(URI resourceUri,URI baseUri)
    throws BackingStoreException
  {
    Preferences prefs=resourceRoot(resourceUri);
    if (baseUri!=null)
    { 
      prefs=new ProxyPreferencesNode
        (prefs,resourceRoot(baseUri)
        );
    }
    return prefs;
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
