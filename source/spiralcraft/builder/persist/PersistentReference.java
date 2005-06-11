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
package spiralcraft.builder.persist;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import spiralcraft.builder.PersistenceException;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.tuple.Tuple;

/**
 * A reference to a persistent object- one with a lifetime
 *   longer than than that its in-vm references, presumably stored on
 *   a non-volatile medium such as a file system or in a database.
 *
 * A PersistentReference is an assocation of an Assembly tree with
 *   a Tuple tree. The Assembly tree determines the code, persistence
 *   directives, and compositional makeup of the persistent object, and the
 *   Tuple tree holds all the persistent data for the object and its
 *   subcomponents. Upon activation of the PersistentReference, the referred
 *   to object will be composed by recursively instantiating the Assembly and
 *   applying the Tuple data to it.
 *
 * The Schema of the Tuples used to persist object data is fully defined by
 *   the Assembly Class URI space- ie. each Tuple's Scheme is derived from a
 *   specific Assembly Class / Java object.
 */
 
public class PersistentReference
  implements Registrant
{
  private final Tuple _tuple;
  private final URI _assemblyClassURI;
  private final AssemblyClass _assemblyClass;
  private Assembly _assembly;
  private RegistryNode _registryNode;
  
  
  public PersistentReference(Tuple tuple)
    throws BuildException
  {
    _tuple=tuple;
    
    _assemblyClassURI=_tuple.getScheme().getURI();
    if (_assemblyClassURI==null)
    { 
      throw new PersistenceException
        ("Object data does not specify a peer Assembly");
    }

    
    _assemblyClass
      =AssemblyLoader.getInstance().findAssemblyClass(_assemblyClassURI);
    if (_assemblyClass==null)
    { 
      throw new PersistenceException
        ("Cannot find Object type '"+_assemblyClassURI+"'");
    }
      
    
  }
  
  public void register(RegistryNode node)
  {
    _registryNode=node;
    _registryNode.registerInstance(PersistentReference.class,this);
  }

  public Tuple getTuple()
  { return _tuple;
  }
  
  public Object get()
    throws BuildException
  { 
    if (_assembly==null)
    {
      _assembly=_assemblyClass.newInstance((Assembly) _registryNode.findInstance(Assembly.class));
      _assembly.register(_registryNode);
    }
    return _assembly.getObject();
  }
  
  public void flush()
    throws PersistenceException
  { 
    
    // XXX Wrap with a transaction so everything can commit at once
    _assembly.storePersistentData();

  }
  
}
