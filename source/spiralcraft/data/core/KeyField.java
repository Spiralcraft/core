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
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;

import spiralcraft.data.lang.DataReflector;


public class KeyField
  extends FieldImpl
{
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
    setType(key.getForeignType());
    super.resolve();
  }
  
  
  @SuppressWarnings("unchecked")
  public Channel<?> bind(Focus<? extends Tuple> focus)
    throws BindException
  { 
    
    Focus keyFocus=new SimpleFocus(focus,key.bind(focus));

    Query query=key.getForeignQuery();
    Focus queryableFocus=focus.findFocus(Queryable.QUERYABLE_URI);
    if (queryableFocus!=null)
    { 
      
      try
      { 
        BoundQuery boundQuery
          =query.getDefaultBinding(keyFocus, (Queryable) queryableFocus.getSubject());
        return new KeyFieldChannel(getType(),boundQuery);
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

    }
    
    public boolean isWritable()
    { return false;
    }

    @Override
    protected DataComposite retrieve()
      throws AccessException
    {
      try
      { return new CursorAggregate(query.execute());
      }
      catch (DataException x)
      { throw new AccessException(x.toString(),x);
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