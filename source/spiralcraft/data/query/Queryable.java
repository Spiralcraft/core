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

import spiralcraft.lang.Focus;

/**
 * <P>A Queryable is anything that can execute a Query and provide a Cursor of
 *   result Tuples.
 * 
 * <P>A Query must be bound before it can be executed. When a Query is bound, data
 *   paths are established from the data sources and all parameter sources.
 *   
 * <P>Once bound, the Query can be repeatedly executed efficiently for different 
 *   parameter values.
 */
public interface Queryable
{

  public BoundQuery bindQuery(Query q,Focus context)
    throws DataException;
}
