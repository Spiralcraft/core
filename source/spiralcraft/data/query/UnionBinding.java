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
import java.util.HashSet;
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
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;

class UnionBinding<Tq extends Union,Tt extends Tuple>
  extends BoundQuery<Tq,Tt>
{
  private List<BoundQuery<?,Tt>> sources
    =new ArrayList<BoundQuery<?,Tt>>();
  private boolean resolved;
  private final boolean debugTrace;
  
  private final boolean debugFine;
  
  
  @SuppressWarnings("unchecked")
  public UnionBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query,paramFocus);
    for (Query sourceQuery : query.getSources())
    { 
      sources.add
      ( (BoundQuery<?,Tt>) 
          (store!=null
              ?store.query(sourceQuery,paramFocus)
              :sourceQuery.bind(paramFocus)
          )
      );      
    }

    debugTrace=debugLevel.canLog(Level.TRACE);
    
    debugFine=debugLevel.canLog(Level.FINE);
    
  }

  public UnionBinding
    (Tq query
    ,List<BoundQuery<?,Tt>> sources
    ,Focus<?> paramFocus
    )
    throws DataException
  { 
    super(query,paramFocus);
    this.sources=sources;
    debugTrace=debugLevel.canLog(Level.TRACE);
    
    debugFine=debugLevel.canLog(Level.FINE);
    
  }
  
  @Override
  public void resolve() throws DataException
  { 
    if (resolved)
    { return;
    }
    resolved=true;
    super.resolve();
    for (BoundQuery<?,?> source : sources)
    { source.resolve();
    }
  }
  
  @Override
  public SerialCursor<Tt> doExecute()
    throws DataException
  {
    if (debugTrace)
    { log.trace(toString()+": Executing Union ");
    }
    return new UnionSerialCursor();
  }
  


  protected class UnionSerialCursor
    implements SerialCursor<Tt>
  {

    private SerialCursor<Tt> currentSource;
    private Iterator<BoundQuery<?,Tt>> sourceIterator;
    private HashSet<Tuple> seen=new HashSet<Tuple>();
    
    
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
          if (!checkDuplicate(currentSource.getTuple()))
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
            if (debugFine)
            { log.fine(toString()+" moving to next source"); 
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
    
    
    private boolean checkDuplicate(Tuple t)
    { 
      if (seen.contains(t))
      { return true;
      }
      else
      { seen.add(t);
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

    @Override
    public Channel<Tt> bind()
      throws BindException
    { return new CursorBinding<Tt,UnionSerialCursor>(this);
    }
    
    @Override
    public void close()
      throws DataException
    { 
      if (currentSource!=null)
      { currentSource.close();
      }
    }
  
  }

}