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
package spiralcraft.data;

/**
 * Describes a unit of structured data. 
 */
public interface Scheme
{

  /**
   *@return the Type associated with this Scheme, or null if there is no
   *  Type associated with this Scheme
   */
  Type getType();
  
  /**
   * A Scheme's archetype is usually equivalent to
   *   the result of getType().getArchetype().getScheme(). Schemes
   *   that do not have Types, however, usually have archetype Schemes that
   *   are associated with a Type.
   *
   *@return Whether this Scheme is derived from the specified Scheme
   */
  boolean hasArchetype(Scheme scheme);
  
  /**
   *@return the Field that corresponds to the specified index. Fields are
   *   assigned indexes in order of their definition in the Scheme.
   */
  Field getFieldByIndex(int index);

  /**
   *@return the Field that corresponds to the specified name. 
   * Field names are unique within a Scheme
   */
  Field getFieldByName(String name);
  
  /**
   *@return the number of Fields in this scheme
   */
  int getFieldCount();
  
  /**
   *@return an Iterable which provides access to 
   *   fields in order of their indexes
   */
  Iterable<? extends Field> fieldIterable();
}
