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
package spiralcraft.builder;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import spiralcraft.prefs.XmlPreferencesFactory;
import spiralcraft.prefs.XmlPreferencesNode;
import spiralcraft.prefs.ProxyPreferencesNode;
import spiralcraft.prefs.BackingStoreFormatException;

import java.net.URI;

/**
 * A persistent object represented in an XML based portable data format.
 *
 * Persistent objects are backed by Assembly Classes, which associate the 
 *   persistence data with Java code in a developer manageable fashion.
 *
 * Persistent objects can have their state saved and restored at runtime.
 *
 * An instance of a persistent object is tied to its non-volatile representation
 *   in a storage medium.
 * 
 * Note: This class currently uses the Java preferences API, but is scheduled
 *   to be converted to use the native Tuple persistence mechanism and will,
 *   as a result, be vastly simplified.
 */
public class XmlObject
  extends PersistentReference
{
  // XXX TO-DO: Convert to use Tuple interface, simplify API
  
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
