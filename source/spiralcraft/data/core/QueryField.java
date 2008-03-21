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


import spiralcraft.lang.AccessException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLogger;


import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.QueryChannel;
import spiralcraft.data.query.Queryable;


public class QueryField
  extends FieldImpl
{
  protected static ClassLogger log=ClassLogger.getInstance(QueryField.class);
  
  private Query query;
  
  
  public QueryField()
  { 
  }
  
  public Query getQuery()
  { return query;
  }
  
  public void setQuery(Query query)
  {
    this.query=query;
    if (getType()==null)
    { setType(Type.getAggregateType((query.getType())));
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
          =((Queryable) queryableFocus.getSubject().get()).query
            (query,focus);
        return new QueryFieldChannel(getType(),boundQuery);
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
  
  private String focusChain(Focus<?> focus)
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
  
  @SuppressWarnings("unchecked")
  public class QueryFieldChannel
    extends AbstractChannel<DataComposite>
  {
    private BoundQuery query;
    
    public QueryFieldChannel(Type<?> type,BoundQuery query)
      throws BindException
    { 
      super(DataReflector.<DataComposite>getInstance(type));
      this.query=query;
    }
    
    public boolean isWritable()
    { return false;
    }

    @Override
    protected DataComposite retrieve()
      throws AccessException
    {
      try
      {
        
      try
      { 
//        log.fine("QueryField "+getURI()+" retrieving...");

        // Trace who is invoking us 
//        new  Exception().printStackTrace();
        
        
        if (getType().isAggregate())
        { 
          SerialCursor cursor=query.execute();
          if (cursor.getResultType()==null)
          {
            log.fine("cursor result type is null "+cursor);
          }
          
          
          CursorAggregate aggregate
            =new CursorAggregate(query.execute());
          // log.fine(aggregate.toString());
          return aggregate;
        }
        else
        { 
          Tuple val=null;
          SerialCursor cursor=query.execute();
          while (cursor.dataNext())
          { 
            if (val!=null)
            { 
              throw new AccessException
                (getURI()+": Cardinality violation: non-aggregate query returned more" +
                " than one result"
                );
            }
            else
            { val=cursor.dataGetTuple();
            }
          }
          // log.fine(val!=null?val.toString():"null");
          return val;
        }
      }
      catch (DataException x)
      { 
        throw new AccessException(x.toString(),x);
      }
      
      }
      catch (RuntimeException x)
      { 
        x.printStackTrace();
        throw x;
      }
    }

    @Override
    protected boolean store(DataComposite val)
      throws AccessException
    { 

      
      throw new AccessException
        ("Can't store anything in a query."
        );
     
    }
    
   
  }

}