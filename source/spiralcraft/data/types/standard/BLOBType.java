//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.types.standard;

import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;

import java.net.URI;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.UnresolvableURIException;

/**
 * Represents a set of bytes referenceable via an IO resource 
 * 
 * @author mike
 */
public class BLOBType
  extends PrimitiveTypeImpl<Resource>
{
  public BLOBType(TypeResolver resolver,URI uri)
  { super(resolver,uri,Resource.class);
  }
  
  public Resource fromString(String str)
    throws DataException
  { 
    if (str==null)
    { return null;
    }
    try
    { return Resolver.getInstance().resolve(URI.create(str));
    }
    catch (UnresolvableURIException x)
    { throw new DataException("Error referencing BLOB",x);
    }
  }

  public String toString(Resource resource)
  { 
    if (resource!=null)
    { return resource.getURI().toString();
    }
    else
    { return null;
    }
  }

}