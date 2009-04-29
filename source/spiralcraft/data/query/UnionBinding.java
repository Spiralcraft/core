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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.CursorBinding;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
//import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;

class UnionBinding<Tq extends Union,Tt extends Tuple>
  extends BoundQuery<Tq,Tt>
{
//  private final Focus<?> paramFocus;
  private SimpleFocus<?> focus;
  private List<BoundQuery<?,Tt>> sources
    =new ArrayList<BoundQuery<?,Tt>>();
  private boolean resolved;
  
//  private Queryable<Tt> store;
  
  @SuppressWarnings("unchecked")
  public UnionBinding
    (Tq query
//    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    for (Query sourceQuery : query.getSources())
    { sources.add((BoundQuery<?,Tt>) store.query(sourceQuery,focus));
    }
//    this.paramFocus=paramFocus;
//    this.store=store;
    setQuery(query);
    
    
  }

  @Override
  public void resolve() throws DataException
  { 
    super.resolve();
    if (!resolved)
    { 
      for (BoundQuery<?,?> source : sources)
      { source.resolve();
      }
      resolved=true;
    }    
  }
  
  @Override
  public SerialCursor<Tt> execute()
    throws DataException
  {
    if (!resolved)
    { resolve();
    }
    return new UnionSerialCursor();
  }
  


  protected class UnionSerialCursor
    implements SerialCursor<Tt>
  {

    private SerialCursor<Tt> currentSource;
    private Iterator<BoundQuery<?,Tt>> sourceIterator;
    
    public UnionSerialCursor()
      throws DataException
    { 
      sourceIterator=sources.iterator();
      if (sourceIterator.hasNext())
      { currentSource=sourceIterator.next().execute();
      }
    }
    
    @Override
    public boolean next()
      throws DataException
    {
      boolean done=false;
      while (!done)
      {
        
        if (currentSource.next())
        { 
          if (!checkDuplicate())
          { return true;
          }
        }
        else
        {
          if (sourceIterator.hasNext())
          { 
            if (currentSource!=null)
            { currentSource.close();
            }
            currentSource=sourceIterator.next().execute();
          }
          else
          { done=true;
          }
        }
      }
      return false;
    }
    
    
    private boolean checkDuplicate()
    { 
      // XXX implement, perhaps using Identifier
      return false;
    }

    @Override
    public FieldSet getFieldSet()
    { return getType().getScheme();
    }

    @Override
    public Tt getTuple()
      throws DataException
    {
      if (currentSource!=null)
      { return currentSource.getTuple();
      }
      else
      { return null;
      }
    }

    @Override
    public Identifier getRelationId()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Type<?> getResultType()
    { return getType();
    }

    public Channel<Tt> bind()
      throws BindException
    { return new CursorBinding<Tt,UnionSerialCursor>(this);
    }
    
    public void close()
      throws DataException
    { 
      if (currentSource!=null)
      { currentSource.close();
      }
    }
  
  }

}