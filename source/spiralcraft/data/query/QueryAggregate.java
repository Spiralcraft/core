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

import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.spi.ListAggregate;

/**
 * An Aggregate backed by a Query
 * 
 * @author mike
 */
public class QueryAggregate<Tq extends Query,Tt extends Tuple>
  extends ListAggregate<Tt>
{

  public QueryAggregate(BoundQuery<Tq,Tt> query)
    throws DataException
  { 
    
    super(Type.resolve(query.getType().getURI().toString().concat(".list")));
    
    
    SerialCursor<Tt> cursor=query.execute();
    while (cursor.dataNext())
    { list.add(cursor.dataGetTuple());
    }
    
  }
  
  
  
}
