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
 * A description of a single element of a Tuple.
 */
public interface Field
{
  /**
   * The Scheme to which this Field belongs
   */
  Scheme getScheme();
  
  /**
   * The index of the Field within the Scheme, which corresponds to the
   *   ordinal position of the associated value within the Tuple
   */
  int getIndex();
  
  /**
   * The name of the Field, to be used for the programmatic binding of
   *   data consumers and producers to Tuples of this Scheme.
   */
  String getName();
  
  /**
   * The Type of the Field
   */
  Type getType();
  
}
