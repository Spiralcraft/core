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
import spiralcraft.lang.SimpleFocus;

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLogger;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;

import spiralcraft.data.lang.DataReflector;


public class KeyField
  extends FieldImpl
{
  protected static final ClassLogger log
    =ClassLogger.getInstance(KeyField.class);
  
  private KeyImpl key;
  
  public KeyField(KeyImpl key)
  { 
    this.key=key;
  }
  
  public KeyImpl getKey()
  { return key;
  }
  
  @Override
  public void resolve()
    throws DataException
  { 
    setName(key.getName());
    
    if (key.getForeignType()==null)
    { throw new DataException("Foreign type is null Key "+key);
    }
    
    if (key.getImportedKey().isUnique())
    { setType(key.getForeignType());
    }
    else
    { setType(Type.getAggregateType(key.getForeignType()));
    }
    super.resolve();
  }
  
  
  @SuppressWarnings("unchecked")
  public Channel<?> bind(Focus<? extends Tuple> focus)
    throws BindException
  { 
    
    Focus keyFocus=new SimpleFocus(focus,key.bind(focus));

    Query query=key.getForeignQuery();
    log.fine("Foreign query is "+query);
    Focus<Queryable> queryableFocus
      =(Focus<Queryable>) focus.findFocus(Queryable.QUERYABLE_URI);
    if (queryableFocus!=null)
    { 
      
      try
      { 
        Queryable queryable=queryableFocus.getSubject().get();
        if (queryable!=null)
        {  
          BoundQuery boundQuery
            = queryable.query(query,keyFocus);
          if (query==null)
          {
            throw new BindException
              ("Got null query from "+queryable+" for query "+query);
          }
          return new KeyFieldChannel(getType(),boundQuery);
          
        }
        else
        { 
          throw new BindException
            ("No Queryable available in Focus chain "+focus.toString());
        }
      }
      catch (DataException x)
      { throw new BindException(x.toString(),x);
      }
    }
    else
    { throw new BindException("No Queryable reachable from Focus "+focus);
    }
  }
  
  
  @SuppressWarnings("unchecked")
  public class KeyFieldChannel
    extends AbstractChannel<DataComposite>
  {
    private BoundQuery query;
    
    public KeyFieldChannel(Type<?> type,BoundQuery query)
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
//        log.fine("KeyField "+getURI()+" retrieving...");
        if (getType().isAggregate())
        { 
          SerialCursor cursor=query.execute();
          if (cursor.getResultType()==null)
          {
            log.fine("cursor result type is null "+cursor);
          }
          
          
          CursorAggregate aggregate
            =new CursorAggregate(query.execute());
//          log.fine(aggregate.toString());
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
//          log.fine(val!=null?val.toString():"null");
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
      // XXX: We may want to re-translate the stored value and update the
      //   key fields
      
      throw new AccessException
        ("Can't store key reference: Referenced object to field value " +
        		"translation not implemented"
        );
     
    }
    
   
  }


}