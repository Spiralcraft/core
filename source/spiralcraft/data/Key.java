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
 * A set of Fields within a Scheme which uniquely identify a single Tuple 
 *   or distinctly identify a group of Tuples within a given Space.
 */
public interface Key
  extends Projection
{

  /**
   * @return The Scheme to which this Key belongs.
   */
  public Scheme getScheme();
  
  /**
   * @return The index of this Key within the set of Keys belonging to
   *   Scheme
   */
  public int getIndex();
  
  /**
   * @return Whether this Key is the primary key for its Scheme
   */
  public boolean isPrimary();
  
  /**
   * @return Whether this Key uniquely identifies a single Tuple
   */
  public boolean isUnique();
  
  /**
   * @return A Type which provides data for this Key's Fields.
   */
  public Type getForeignType();
  
  /**
   * @return A Key from the foreign Type that originates the data values
   *   for this Key's Fields. 
   */
  public Key getImportedKey();
  
}
