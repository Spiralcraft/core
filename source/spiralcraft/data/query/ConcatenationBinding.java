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

public class ConcatenationBinding<Tq extends Concatenation,Tt extends Tuple>
  extends BoundQuery<Tq,Tt>
{
  private SimpleFocus<?> focus;
  private List<BoundQuery<?,Tt>> sources
    =new ArrayList<BoundQuery<?,Tt>>();
  private boolean resolved;
  
  
  @SuppressWarnings("unchecked")
  public ConcatenationBinding
    (Tq query
    ,Queryable<?> store
    )
    throws DataException
  { 
    for (Query sourceQuery : query.getSources())
    { sources.add((BoundQuery<?,Tt>) store.query(sourceQuery,focus));
    }
    setQuery(query);
  }
  
  @SuppressWarnings("unchecked")
  public ConcatenationBinding(List<BoundQuery<?,Tt>> boundQueries)
  {
    Tq query=(Tq) new Concatenation();
    for (BoundQuery<?,Tt> boundQuery : boundQueries)
    { 
      query.addSource(boundQuery.getQuery());
      sources.add(boundQuery);
    }
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
    return new ConcatenationSerialCursor();
  }
  


  protected class ConcatenationSerialCursor
    implements SerialCursor<Tt>
  {

    private SerialCursor<Tt> currentSource;
    private Iterator<BoundQuery<?,Tt>> sourceIterator;
    
    public ConcatenationSerialCursor()
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
        { return true;
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
    { return new CursorBinding<Tt,ConcatenationSerialCursor>(this);
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