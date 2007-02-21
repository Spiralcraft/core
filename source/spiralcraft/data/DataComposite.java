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
 * An Object which holds data. Common superclass of Tuple and Aggregate.
 */
public interface DataComposite
{
  /**
   * The Type of data this object represents.
   */
  Type<?> getType();
  
  /**
   *@return Whether this DataComposite is a Tuple.
   */
  boolean isTuple();

  /**
   *@return Whether this DataComposite in an Aggregate.
   */
  boolean isAggregate();

  /**
   *@return This object as a Tuple
   */
  Tuple asTuple();
  
  /**
   *@return This object as an Aggregate
   */
  Aggregate asAggregate();
  
  /**
   *@return A recursively generated textual representation of this data, for
   *  diagnostic and debugging purposes.
   */
  String toText(String indent)
    throws DataException;
  
}