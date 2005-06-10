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

/**
 * Thrown when a problem is experiences saving or restoring a persistent
 *   object
 */
public class PersistenceException
  extends BuildException
{
  // XXX TO-DO: Move to spiralcraft.builder.persist
  
  public PersistenceException(String message)
  { super(message);
  }

  public PersistenceException(String message,Throwable nested)
  { super(message,nested);
  }
}
