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
package spiralcraft.data.query;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;



import spiralcraft.lang.Focus;

import java.util.List;

/**
 * <P>A Queryable is anything that can execute a Query and provide a Cursor of
 *   result Tuples.
 * 
 * <P>A Query must be bound before it can be executed. When a Query is bound,
 *   data paths are established from the data sources and all parameter
 *   sources.
 *   
 * <P>Once bound, the Query can be repeatedly executed efficiently for
 *   different parameter values.
 */
public interface Queryable
{

  /**
   * @return An array of all the Types supported by this Queryable
   */
  Type[] getTypes();
  
  /**
   * @return Whether the store contains the specified Type.
   */
  boolean containsType(Type type);
  
  /**
   * Returns all data instances of a specific Type for further manipulation by
   *   BoundQueries. This method is used when a Queryable cannot provide
   *   an optimized implementation for the simplest of Queries, or if no 
   *   optimization is needed. 
   * 
   * @return A boundQuery which provides the set of all instances for a given
   *   type. 
   */
  BoundQuery getAll(Type type) 
    throws DataException;
  

  /**
   * Bind the specified Query and parameter context to this Queryable.
   * 
   *@return a BoundQuery that implements the data flow path and provides the
   *  data requested by the Query
   */
  BoundQuery query(Query q,Focus context)
    throws DataException;
}
