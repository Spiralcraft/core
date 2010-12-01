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

import spiralcraft.data.query.Query;
import spiralcraft.util.KeyFunction;
import spiralcraft.util.string.StringConverter;

/**
 * A set of Fields within a Scheme which uniquely identify a single Tuple 
 *   or distinctly identify a group of Tuples within a given Space.
 */
public interface Key<T>
  extends Projection<T>
{

  /**
   * <p>A Field name to access data referred to by the Key. A new Field will
   *   be added to the Scheme automatically.
   * </p>
   *  
   * @return
   */
  public String getName();
  
  /**
   * @return The Scheme to which this Key belongs.
   */
  public Scheme getScheme();
  
  
  /**
   * @return Whether this Key is the primary key for its Scheme
   */
  public boolean isPrimary();
  
  /**
   * 
   * @return The title of this Key to appear in a UI
   */
  public String getTitle();
  
  /**
   * @return Whether this Key uniquely identifies a single Tuple
   */
  public boolean isUnique();
  
  /**
   * @return A Type which provides data for this Key's Fields.
   */
  public Type<?> getForeignType();
  
  /**
   * @return A Key from the foreign Type that originates the data values
   *   for this Key's Fields. 
   */
  public Key<?> getImportedKey();
  
  /**
   * @return The Query that will return the Tuple or Tuples when provided with
   *   the matching Field values.
   */
  public Query getQuery();

  /**
   * 
   * @return The names of the Fields that make up this Key definition
   */
  @Override
  public String[] getFieldNames();
  
  /**
   * 
   * @return A KeyFunction that returns the projected key value for a
   *   Tuple
   */
  public KeyFunction<KeyTuple,T> getFunction();
  
  /**
   * 
   * @return The default set of String converters for the Key elements
   */
  public StringConverter<?>[] getStringConverters();
  
}
