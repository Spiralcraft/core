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

import spiralcraft.tuple.SchemeResolver;
import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.TupleException;

import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import java.net.URI;

import java.util.HashMap;

/**
 * Resolves Schemes derived from the application codebase
 *   data model via AssemblyClasses.
 */
public class AssemblyClassSchemeResolver
  implements SchemeResolver
{
  private HashMap<URI,Scheme> schemeMap=new HashMap<URI,Scheme>();
  
  public synchronized Scheme resolveScheme(URI uri)
    throws TupleException
  { 
    try
    {
      Scheme scheme=schemeMap.get(uri);
      if (scheme==null)
      {
        
        AssemblyClass assemblyClass
          =AssemblyLoader.getInstance().findAssemblyClass(uri);
        
        if (assemblyClass!=null)
        {
          scheme=new AssemblyClassScheme(uri,assemblyClass);
          schemeMap.put(uri,scheme);
        }
      }
      return scheme;
    }
    catch (BuildException x)
    { throw new TupleException("Error resolving Scheme "+uri,x);
    }
  }
  
}