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

/**
 * <P>A Query is a request for a subset of Tuples from the set of data reachable
 *    by a Queryable. 
 * 
 * <P>It is composed of a tree of operations (sub-Queries) that constrain the set of
 *    reachable data to produce a desired result.
 * 
 * <P>A Query must be bound against a Queryable before it can be executed, which
 *    creates a data path.
 * 
 */
public abstract class Query
{ 
  
}
