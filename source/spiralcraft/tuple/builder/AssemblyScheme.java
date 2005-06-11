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
package spiralcraft.tuple.builder;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import spiralcraft.tuple.Scheme;

import spiralcraft.tuple.spi.SchemeImpl;

import java.net.URI;

/**
 * A Scheme explicitly constructed using an AssemblyClass definition, where
 *   the Scheme, Entity, Field, etc. Objects are explicity described.
 *
 * AssemblyClasses can instantiate interfaces as well as classes. When an
 *   interface is instantiated, a proxy object is created which implements
 *   the interface and implements bean accessors using Tuple storage.
 *
 * A deep copy is made of the Scheme contained in an Assembly. For ease of
 *   writing these Assemblies, it is assumed that the Scheme found in the 
 *   Assembly is not necessarily an efficient implmentation (typically
 *   the Scheme found in the Assembly will be a proxy implementation of
 *   the Scheme interface and related interfaces).
 *
 */
public class AssemblyScheme
  extends SchemeImpl
{
  
  public AssemblyScheme(URI uri)
    throws BuildException
  { super(loadScheme(uri));
  }

  private static final Scheme loadScheme(URI uri)
    throws BuildException
  {
    AssemblyClass assemblyClass
      =AssemblyLoader.getInstance().findAssemblyDefinition(uri);
    
    Assembly assembly
      =assemblyClass.newInstance(null);
    
    return (Scheme) assembly.getSubject().get();
  }
}
