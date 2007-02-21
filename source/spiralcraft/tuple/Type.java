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
package spiralcraft.tuple;

/**
 * Describes the data type of a data element. 
 *
 * Data of any Type has a representation which corresponds to single Java
 *   class.
 *
 * Compound Types are associated with a Scheme which describes the
 *   the Fields of compound data, which is normally represented by a Tuple.
 */
public interface Type
{
  
  /**
   * The Scheme of the compound data for this Type.
   */
  Scheme getScheme();
  
  /**
   * The public Java class or interface used to access the data element
   */
  Class<?> getJavaClass();
}
