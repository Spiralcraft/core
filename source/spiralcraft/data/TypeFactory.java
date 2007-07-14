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
 * Creates Type objects for a specific namespace
 */
public interface TypeFactory
{
  /**
   *@return A new instance of the specified Type that is not yet linked,
   *  or null, if this TypeFactory does not apply to the specified URI.
   */
  public Type<?> createType(TypeResolver resolver,URI typeURI)
    throws DataException;
  
}