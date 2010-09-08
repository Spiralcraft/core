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

import spiralcraft.lang.Expression;

/**
 * Describes a unit of structured data as a collection of Fields, Keys
 *   Methods, and Constraints.
 */
public interface Scheme
  extends FieldSet
{
  
  /**
   * A Scheme's archetype is usually equivalent to
   *   the result of getType().getArchetype().getScheme().<P>
   *
   *@return Whether this Scheme is derived from the specified Scheme
   */
  boolean hasArchetype(Scheme scheme);
  
  /**
   *@return the Field that corresponds to the specified index. Fields are
   *   assigned indexes in order of their definition in the Scheme.
   */
  @Override
  <X> Field<X> getFieldByIndex(int index);

  /**
   *@return the Field that corresponds to the specified name. 
   * Field names are unique within a Scheme. 
   */
  @Override
  <X> Field<X> getFieldByName(String name);
  
  /**
   *@return the number of Fields in this scheme
   */
  @Override
  int getFieldCount();
  
  /**
   *@return an Iterable which provides access to 
   *   fields in order of their indices
   */
  @Override
  Iterable<? extends Field<?>> fieldIterable();
  
  /**
   *@return The primary key for this Scheme. 
   */
  Key<Tuple> getPrimaryKey();
  
  /**
   * @return The Key at the specified index
   */
  Key<Tuple> getKeyByIndex(int index);
  
  /**
   * <p>The Projection signature is the set of Field expressions which define
   *   a given Projection in reference to this type.
   * </p>
   * 
   * @param signature
   * @return the Key signature
   */
  Projection<Tuple> getProjection(Expression<?>[] signature)
    throws DataException;
  
  /**
   * @returnn Iterable which provides access to 
   *   keys in the order in which they were defined
   */
  Iterable<? extends Key<Tuple>> keyIterable();
  
  /**
   * @return The number of keys that have been defined for this Scheme
   */
  int getKeyCount();
  
  
}
