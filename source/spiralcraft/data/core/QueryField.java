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
package spiralcraft.data.core;


import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;

import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.QueryChannel;
import spiralcraft.data.query.Queryable;


public class QueryField
  extends FieldImpl
{
  private Query query;
  private boolean resolved;
  
  public QueryField()
  { 
  }
  
  public Query getQuery()
  { return query;
  }
  
  public void setQuery(Query query)
  {
    this.query=query;
    if (getType()!=null)
    { setType(query.getType());
    }
  }
  

  
  @Override
  public void subclassResolve()
  {
  }
  
  
  @SuppressWarnings("unchecked")
  public Channel<?> bind(Focus<? extends Tuple> focus)
    throws BindException
  { 
    
    Focus queryableFocus=focus.findFocus(Queryable.QUERYABLE_URI);
    if (queryableFocus!=null)
    { 
      
      try
      { 
        BoundQuery boundQuery
          =query.bind(focus, (Queryable) queryableFocus.getSubject().get());
        return new QueryChannel(boundQuery);
      }
      catch (DataException x)
      { throw new BindException(x.toString(),x);
      }
    }
    else
    { 
      throw new BindException
        ("No Queryable reachable from Focus "+focusChain(focus));
    }
  }
  
  private String focusChain(Focus focus)
  { 
    StringBuilder buf=new StringBuilder();
    buf.append("[");
    while (focus!=null)
    { 
      buf.append("\r\n");
      buf.append(focus.toString());
      focus=focus.getParentFocus();
    }
    buf.append("\r\n");
    buf.append("]");
    return buf.toString();
  }
  

}