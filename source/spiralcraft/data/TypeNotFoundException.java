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
package spiralcraft.data;

import java.net.URI;

/**
 * Throws when a Type is not found by the TypeResolver
 */
public class TypeNotFoundException
  extends DataException
{
  private static final long serialVersionUID=1;	

  public TypeNotFoundException(URI typeURI)
  { super("Type not found: "+typeURI.toString());
  }

  public TypeNotFoundException(URI typeURI,Throwable reason)
  { super("Type "+typeURI.toString()+" not found: "+reason.toString(),reason);
  }
  
  public TypeNotFoundException(String dependencyMessage,TypeNotFoundException reason)
  { super(dependencyMessage,reason);
  }

  /**
   * 
   * @return A cause that is not a TypeNotFoundException, such as an error processing
   *   a Type definition, or null for the typical case where a Type definition was not
   *   found where expected.
   */
  public Throwable getExternalCause()
  { 
    if (getCause()!=null && getCause() instanceof TypeNotFoundException)
    { return ((TypeNotFoundException) getCause()).getExternalCause();
    }
    return null;
  }
}
