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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.TeleFocus;
import spiralcraft.log.ClassLogger;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;

/**
 * A Query operation which constrains the result of another Query
 */
public class Selection
  extends Query
{
 
  private Expression<Boolean> constraints;
  
  public Selection()
  {
  }
  
  /**
   * Construct a Selection which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public Selection(Query source,Expression<Boolean> constraints)
  { 
    this.constraints=constraints;
    addSource(source);
  }
  
  public FieldSet getFieldSet()
  { 
    if (sources.size()>0)
    { return sources.get(0).getFieldSet();
    }
    else
    { return null;
    }
  }
    
  public Selection
      (Selection baseQuery
      ,Expression<Boolean> constraints
      )
  { 
    super(baseQuery);
    this.constraints=constraints;
  }

  
  /**
   * Specify the Expression which constrains the result
   */
  public void setConstraints(Expression<Boolean> constraints)
  { this.constraints=constraints;
  }
 
  public void setSource(Query source)
  { 
    type=source.getType();
    addSource(source);
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public Expression<Boolean> getConstraints()
  { return constraints;
  }
  

  
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new SelectionBinding<Selection,T>(this,focus,store);
   
  }
  
  public String toString()
  { return super.toString()
      +"[constraints="+constraints+"]: sources="
      +getSources().toString();
  }
    
}

class SelectionBinding<Tq extends Selection,Tt extends Tuple>
  extends UnaryBoundQuery<Tq,Tt,Tt>
{
  private static final ClassLogger log=ClassLogger.getInstance(SelectionBinding.class);

  private final Focus<?> paramFocus;
  private Focus<Tt> focus;
  private Channel<Boolean> filter;
  private boolean resolved;
  
  public SelectionBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query.getSources(),paramFocus,store);
    setQuery(query);
    this.paramFocus=paramFocus;
    
  }

  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
    

      focus= new TeleFocus<Tt>(paramFocus,sourceChannel);
      
      if (debug)
      { log.fine("Binding constraints "+getQuery().getConstraints());
      }
      
      try
      { 
        filter=focus.<Boolean>bind(getQuery().getConstraints());
        if (debug)
        { filter.setDebug(true);
        }
      }
      catch (BindException x)
      { throw new DataException("Error binding constraints "+x,x);
      }
      resolved=true;
    }
  }
  

  protected SerialCursor<Tt> newSerialCursor(SerialCursor<Tt> source)
    throws DataException
  { return new SelectionSerialCursor(source);
  }
  
  protected ScrollableCursor<Tt> 
    newScrollableCursor(ScrollableCursor<Tt> source)
    throws DataException
  { return new SelectionScrollableCursor(source);
  }

  class SelectionSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    public SelectionSerialCursor(SerialCursor<Tt> source)
      throws DataException
    { super(source);
    }
  
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
        if (debug)
        { log.fine(toString()+"BoundSelection: eod ");
        }
        return false;
      }
    
      if (filter.get())
      {  
        if (debug)
        { log.fine(toString()+"BoundSelection: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
        if (debug)
        { log.fine(toString()+"BoundSelection: filtered "+t);
        }
        return false;
      }
    }
    
    public Type<?> getResultType()
    { return sourceCursor.getResultType();
    }
  }

  class SelectionScrollableCursor
    extends UnaryBoundQueryScrollableCursor
  {
    public SelectionScrollableCursor(ScrollableCursor<Tt> source)
      throws DataException
    { super(source);
    }

    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
        if (debug)
        { log.fine("BoundSelection: eod ");
        }
        return false;
      }

      if (filter.get())
      {  
        if (debug)
        { log.fine("BoundSelection: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
        if (debug)
        { log.fine("BoundSelection: filtered "+t);
        }
        return false;
      }
    }

    public Type<?> getResultType()
    { return sourceCursor.getResultType();
    }
  }
}