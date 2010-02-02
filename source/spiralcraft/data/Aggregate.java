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

import spiralcraft.common.Indexable;


/**
 * <P>Holds a random-access collection of objects of a common type.
 */
public interface Aggregate<T>
  extends DataComposite,Indexable<T>
{
  
  /**
   * Obtain the value at the specified index of this aggregate
   */
  T get(int index);
  
  /**
   * Indicate whether the value returned by the get(int index) method may
   *   change at some point in the future. Some elements of data processing
   *   functionality may require that an Aggregate be immutable before 
   *   processing.
   */
  boolean isMutable();
  
  /**
   * @return an immutable snapshot copy of this Aggregate.
   */
  Aggregate<T> snapshot()
    throws DataException;
  
  /**
   * The number of elements in this Aggregate
   */
  public int size();
 
  /**
   *
   * @param projection
   * @return The Index associated with the specified Projection, optionally
   *   creating it.
   */
  public Index<T> getIndex(Projection<T> projection,boolean create)
    throws DataException;
  
  /**
   * <p>A mapping of the elements in the List according to a key defined by a
   *    Projection.
   * </p>
   * 
   */
  public interface Index<T>
  {
    public Aggregate<T> get(KeyTuple key);
    
    public T getOne(KeyTuple key);
  }
}