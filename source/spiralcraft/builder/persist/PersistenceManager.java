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

import spiralcraft.tuple.Tuple;

import spiralcraft.tuple.sax.TupleReader;

import spiralcraft.sax.ParseFactory;

import spiralcraft.builder.BuildException;
import spiralcraft.builder.PersistenceException;

import java.net.URI;

import java.util.List;

/**
 * Manages the lifecycle of PersistentReferences.
 * 
 * State: Initial Development
 */
public class PersistenceManager
{
  private final AssemblyClassSchemeResolver resolver
    =new AssemblyClassSchemeResolver();
    
  /**
   * Activate the PersistentReference stored at the given location 
   */
  public PersistentReference activate(URI storeName)
    throws BuildException
  { 
    try
    {
      // XXX The PersistenceManager should be instantiated with a
      // XXX   Store which abstracts the storage mechanism (ie. this
      // XXX   method should not know about XML. 
      TupleReader reader=new TupleReader(resolver,null);
      new ParseFactory().parseURI
        (storeName
        ,reader
        );
      
      List<Tuple> list=reader.getTupleList();
      if (list.size()>0)
      { return new PersistentReference(list.get(0));
      }
      else
      { throw new PersistenceException("No object data in "+storeName);
      }
    }
    catch (BuildException x)
    { throw x;
    }
    catch (Throwable x)
    { throw new PersistenceException("Error resolving object URI "+storeName+":",x);
    }    
  }
  
  /**
   * Deactive the specified PersistentReference. The persistence data
   *   will be flushed and the reference will no longer be accessible.
   */
  public void deactivate(PersistentReference reference)
  {
  }

}
