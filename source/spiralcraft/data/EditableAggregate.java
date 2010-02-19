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

import java.util.Iterator;

/**
 * Holds a aggregation of objects of a common type.
 */
public interface EditableAggregate<T>
  extends Aggregate<T>
{

  /**
   * Add an Object to this aggregation
   * 
   * @param val
   */
  void add(T val);
  
  /**
   * Add all the values in the specified aggregate to this aggregation
   *
   * @param values
   */
  void addAll(Aggregate<T> values);

  /**
   * Add all the values in the specified iteration to this aggregation
   * @param values
   */
  void addAll(Iterator<T> values);
  
  /**
   * Remove the specifed value from this aggregation
   * 
   * @param val
   */
  void remove(T val);
  
  /**
   * Remove all values from this aggregation
   */
  void clear();
}