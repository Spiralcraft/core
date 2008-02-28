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

/**
 * <p>Identifies a DataComposite. Two DataComposites that have the same
 *   identity represent different versions of the same conceptual entity.
 * </p>
 * 
 * <p>Identifiers are commonly implemented as primary key values (Tuples), 
 *   equijoin values for Aggregates (also Tuples) or as wrappers for opaque 
 *   data where the identity is assigned by a container and is not based on 
 *   key fields.
 * </p>
 *   
 * @author mike
 *
 */
public interface Identifier
{
  /**
   * @return The data Type of the DataComposite being identified.
   */
  Type<?> getIdentifiedType();
  
  /**
   * Create a copy of this identifier that is not referenced by the original.
   * 
   * @return
   */
  Identifier copy();
  
  /**
   * Indicate whether the specified identifier refers to the same conceptual
   *   entity as the specified identifier.
   *   
   * @param identifier
   * @return
   */
  boolean equals(Identifier identifier);
  
  
  
  
}
