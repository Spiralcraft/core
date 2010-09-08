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
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ClosureFocus;


import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;

import spiralcraft.data.core.FieldImpl;

import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;


public class QueryField
  extends FieldImpl<DataComposite>
{
  
  private Query query;
//  private boolean resolved;
  
  { setTransient(true);
  }
  
  public QueryField()
  { 
  }
  
  public Query getQuery()
  { return query;
  }
  
  public void setQuery(Query query)
  {
    this.query=query;
  }
  
  @Override
  public boolean isFunctionalEquivalent(Field<?> field)
  { 
    return
      field instanceof QueryField
      && (query!=null
          ?query.equals(((QueryField) field).getQuery())
          :((QueryField) field).getQuery()==null
         )
      && super.isFunctionalEquivalent(field);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Cast type
  @Override
  public void resolveType()
    throws DataException
  {
    if (query!=null)
    { 
      query.resolve();
      if (getType()==null && query.getType()!=null)
      { setType((Type) Type.getAggregateType((query.getType())));
      }
    }
    else
    { throw new DataException("Missing query in field "+toString());
    }
    super.resolveType();
  }
  
  
  

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Channel<DataComposite> bindChannel
    (Channel<Tuple> source
    ,Focus<?> argFocus
    ,Expression<?>[] args
    )
    throws BindException
  { 
    
    // Use original binding context, never bind source expression in
    //   argument context
    Focus<?> context=source.getContext();
    if (context==null)
    { 
      throw new BindException
        ("No context for "+this.getURI()+" "+source);
      // context=argFocus;
    }

    if (!context.isContext(source))
    { context=context.chain(source);
    }
    
    Focus queryableFocus=context.findFocus(Queryable.QUERYABLE_URI);
    if (queryableFocus!=null)
    { 
      
      try
      { 
        ClosureFocus<?> closure
          =new ClosureFocus(context,source);
        BoundQuery boundQuery
          =((Queryable) queryableFocus.getSubject().get()).query
            (query,closure);
        
        boundQuery.resolve();
        return new QueryFieldChannel(getType(),boundQuery,closure);
      }
      catch (DataException x)
      { throw new BindException(x.toString(),x);
      }
    }
    else
    { 
      throw new BindException
        ("No Queryable reachable from Focus "+context.getFocusChain());
    }
  }
  
  @SuppressWarnings({"unchecked","rawtypes"})
  public class QueryFieldChannel
    extends AbstractChannel<DataComposite>
  {
    private BoundQuery boundQuery;
    private ClosureFocus<?> focus;
        
    public QueryFieldChannel(Type<?> type,BoundQuery query,ClosureFocus<?> focus)
      throws BindException
    { 
      super(DataReflector.<DataComposite>getInstance(type));
      this.boundQuery=query;
      this.focus=focus;
      this.context=focus;
    }
    
    @Override
    public boolean isWritable()
    { return false;
    }

    @Override
    protected DataComposite retrieve()
      throws AccessException
    {

      focus.push();
      try
      { 
//        log.fine("QueryField "+getURI()+" retrieving...");

        // Trace who is invoking us 
//        new  Exception().printStackTrace();
        
        
        if (getType().isAggregate())
        { 
          SerialCursor cursor=boundQuery.execute();
          try
          {
            if (cursor.getResultType()==null)
            { 
              log.fine("Field "+getURI()
                +": cursor result type is null "+cursor+" from "+boundQuery
                );
            }
          
          
            CursorAggregate aggregate
              =new CursorAggregate(cursor);
          // log.fine(aggregate.toString());
            return aggregate;
          }
          finally
          { cursor.close();
          }
        }
        else
        { 
          Tuple val=null;
          SerialCursor cursor=boundQuery.execute();
          try
          {
            while (cursor.next())
            { 
              if (val!=null)
              { 
                throw new AccessException
                  (getURI()
                  +": Cardinality violation: non-aggregate query returned more"
                  +" than one result: "
                  +(debug
                   ?"\r\n    A:"+val
                      +"\r\n    B:"+cursor.getTuple()
                   :"\r\n    A:"+val.getType().getURI()
                      +"\r\n    B:"+cursor.getTuple().getType().getURI()
                   )
                  );
              }
              else
              { val=cursor.getTuple();
              }
            }
          }
          finally
          { cursor.close();
          }
          
          // log.fine(val!=null?val.toString():"null");
          return val;
        }
      }
      catch (RuntimeException x)
      { 
        x.printStackTrace();
        throw x;
      }
      catch (DataException x)
      { 
        throw new AccessException(x.toString(),x);
      }
      finally
      { focus.pop();
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