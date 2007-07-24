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
package spiralcraft.data.session;

import spiralcraft.data.query.Queryable;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.CursorBinding;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import java.net.URI;

/**
 * A View based solely on a Queryable.
 * 
 * @author mike
 *
 */
public class SimpleView<T extends Tuple>
  extends View<T>
{
  
  // private URI typeURI;
  
  public void setQueryable(Queryable<Tuple> queryable)
  { this.queryable=queryable;
  }
  
  public void setTypeURI(URI typeURI)
  { 
    try
    { this.type=Type.resolve(typeURI);
    }
    catch (DataException x)
    { 
      throw new IllegalArgumentException
        ("Error resolving type "+typeURI+" : "+x,x);
    }
  }
  
  @Override
  public void bindData(Focus<?> focus)
    throws DataException
  { 

    try
    { setTupleBinding(new CursorBinding<Tuple>(getType().getScheme()));
    }
    catch (BindException x)
    { 
      throw new DataException
        ("Error creating CursorBinding for View '"+getName()+"': "+x,x);
    }
  }

  @Override
  public SerialCursor<Tuple> scan()
    throws DataException
  { return queryable.getAll(getType()).execute();
  }
}
