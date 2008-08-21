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
 * An ordered set of uniquely named Fields which define the data structure of
 *   a Tuple.
 */
public interface FieldSet
{
  /**
   * @return The Type that this FieldSet belongs to, if any.
   */
  Type<?> getType();
  
  /**
   *@return the Field at the specified position in the FieldSet.
   */
  <X> Field<X> getFieldByIndex(int index);

  /**
   *@return the Field in the FieldSet that corresponds to the specified name.
   * Field names are unique within a FieldSet
   */
  <X> Field<X> getFieldByName(String name);
  
  /**
   *@return the number of Fields in this FieldSet
   */
  int getFieldCount();
  
  /**
   *@return an Iterable which provides access to 
   *   fields in order of their indexes
   */
  Iterable<? extends Field<?>> fieldIterable();

}
