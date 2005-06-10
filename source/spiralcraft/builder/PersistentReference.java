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

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
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
 *
 * Note: This mechanism has been deprecated in favor of the
 *  spiralcrat.builder.persist package.
 */

// XXX
// XXX TO-DO: Move all persistent reference code to non-preferences XML format
// XXX

public class PersistentReference
  implements Registrant
{
  private final Preferences _preferences;
  private final String _assemblyClassURI;
  private final AssemblyClass _assemblyClass;
  private Assembly _assembly;
  private RegistryNode _registryNode;
  
  
  public PersistentReference(Preferences prefs)
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
      _assemblyClass=AssemblyLoader.getInstance().findAssemblyDefinition(uri);
      if (_assemblyClass==null)
      { throw new PersistenceException("AssemblyClass definition '"+uri+"' not found");
      }
      
    }
    catch (URISyntaxException x)
    { 
      throw new PersistenceException
        ("Bad assemblyClass name '"+_assemblyClassURI+"'",x);
    }
    
  }
  
  public void register(RegistryNode node)
  {
    _registryNode=node;
    _registryNode.registerInstance(Preferences.class,_preferences);
  }
  
  public Object get()
    throws BuildException
  { 
    if (_assembly==null)
    {
      _assembly=_assemblyClass.newInstance((Assembly) _registryNode.findInstance(Assembly.class));
      _assembly.register(_registryNode.createChild("instance"));
    }
    return _assembly.getSubject().get();
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
