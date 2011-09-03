//
// Copyright (c) 1998,2008 Michael Toth
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
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.SerialCursor;

import spiralcraft.lang.Focus;

/**
 * A Query which provides access to all instances of a given Type. This is usually the 
 *   eventual upstream source for all Queries. 
 */
public class Scan
  extends Query
{
  
  { mergeable=true;
  }
  
  /**
   * Construct an unconfigured Scan
   */
  public Scan()
  { }
  
  /**
   * Construct a new Scan for the given Type
   */
  public Scan(Type<?> type)
  { this.type=type;
  }
  
  public Scan(Query baseQuery)
  { super(baseQuery);
  }
  
  /**
   * Specify the Type whos instances will be retrieved.
   */
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  /**
   * @return the Type whos instances will be retrieved
   */
  @Override
  public Type<?> getType()
  { return type;
  }
  
  @Override
  public FieldSet getFieldSet()
  { 
    if (type!=null)
    { 
      type.link();
      return type.getScheme();
    }
    else
    { return null;
    }
  }


  @Override
  @SuppressWarnings("unchecked")  
  public <T extends Tuple> BoundQuery<?,T> 
    getDefaultBinding(Focus<?> focus,Queryable<?> queryable)
    throws DataException
  { 
    if (queryable==null)
    { 
      throw new DataException
        ("No Queryable available for scan of type "+type.getURI()
          +": "+focus.getFocusChain()
        ); 
    }
    if (conditionX!=null)
    { return new BoundScan<Scan,T>(this,focus,queryable);
    }
    else
    { return (BoundQuery<?,T>) queryable.getAll(type);
    }
  }
  
  
  @Override
  public String toString()
  { return super.toString()+"[type="+getType()+"]";
  }
}

class BoundScan<Tq extends Scan,Tt extends Tuple>
  extends BoundQuery<Tq,Tt>
{

  public BoundQuery<?,Tt> delegate;
  
  @SuppressWarnings("unchecked")
  public BoundScan
  (Tq query
  ,Focus<?> paramFocus
  ,Queryable<?> store
  )
    throws DataException
  { 
    super(query,paramFocus);
    delegate=(BoundQuery<Tq,Tt>) store.getAll(query.getType());
  }

  @Override
  protected SerialCursor<Tt> doExecute()
    throws DataException
  { return delegate.execute();
  }
  
  
}




