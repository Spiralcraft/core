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

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;

import spiralcraft.data.lang.DataReflector;


public class KeyField<T extends DataComposite>
  extends FieldImpl<T>
{
  
  private KeyImpl<Tuple> key;
  
  public KeyField(KeyImpl<Tuple> key)
  { 
    this.key=key;
  }
  
  public KeyImpl<?> getKey()
  { return key;
  }
  
  @SuppressWarnings("unchecked") // Key.getForeignType() is not generic
  @Override
  public void resolve()
    throws DataException
  { 
    setName(key.getName());
    
    if (key.getForeignType()==null)
    { throw new DataException("Foreign type is null Key "+key);
    }
    
    if (key.getImportedKey().isUnique())
    { setType((Type) key.getForeignType());
    }
    else
    { setType((Type) Type.getAggregateType(key.getForeignType()));
    }
    if (key.getForeignQuery()!=null)
    { key.getForeignQuery().resolve();
    }
    super.resolve();
  }
  
  @Override
  public boolean isFunctionalEquivalent(Field<?> field)
  { return false;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Channel<T> bindChannel(Focus<Tuple> focus)
    throws BindException
  { 
    
    Focus<Tuple> keyFocus=new SimpleFocus(focus,key.bindChannel(focus));

    Query query=key.getForeignQuery();
    if (debug)
    { log.fine("Foreign query is "+query);
    }
    Focus<Queryable> queryableFocus
      =focus.<Queryable>findFocus(Queryable.QUERYABLE_URI);
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
          boundQuery.resolve();
          return new KeyFieldChannel(getType(),boundQuery,keyFocus.getSubject());
          
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
    extends AbstractChannel<T>
  {
    private final BoundQuery query;
    
    private final Channel<Tuple> keyChannel;
    
    public KeyFieldChannel
      (Type<T> type,BoundQuery query,Channel<Tuple> keyChannel)
      throws BindException
    { 
      super(DataReflector.<T>getInstance(type));
      this.query=query;
      this.keyChannel=keyChannel;
    }
    
    @Override
    public boolean isWritable()
    { return false;
    }

    @Override
    protected T retrieve()
      throws AccessException
    {
      // Make sure parameter fields are not null
      Tuple keyVal=keyChannel.get();
      if (keyVal==null)
      { 
        
        if (debug)
        { log.fine("Key value is null for "+getURI());
        }
        return null;
      }
      
      int count=keyVal.getFieldSet().getFieldCount();
      for (int i=0;i<count;i++)
      { 
        try
        {
          if (keyVal.get(i)==null)
          { 
            if (debug)
            { 
              log.fine
                ("Key field '"
                 +keyVal.getFieldSet().getFieldByIndex(i).getName()
                 +"' value is null for "+getURI()
                );
            }
          
            return null;
          }
        }
        catch (DataException x)
        { throw new AccessException("Error reading key value for "+getURI());
        }
      }
      
      try
      {
        
      try
      { 
//        log.fine("KeyField "+getURI()+" retrieving...");
        if (getType().isAggregate())
        { 
          SerialCursor cursor=query.execute();
          try
          {
            if (cursor.getResultType()==null)
            { log.fine("cursor result type is null "+cursor);
            }
          
          
            CursorAggregate aggregate
              =new CursorAggregate(cursor);
//            log.fine(aggregate.toString());
            return (T) aggregate;
          }
          finally
          { cursor.close();
          }
          
        }
        else
        { 
          Tuple val=null;
          SerialCursor cursor=query.execute();
          try
          {
            while (cursor.next())
            { 
              if (val!=null)
              { 
                throw new AccessException
                  (getURI()+": Cardinality violation: non-aggregate query returned more" +
                  " than one result"
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
//          log.fine(val!=null?val.toString():"null");
          return (T) val;
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