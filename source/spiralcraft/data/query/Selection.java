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
import spiralcraft.lang.SimpleFocus;
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
  private static final ClassLogger log=ClassLogger.getInstance(Selection.class);
  
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
  
  
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<T> store)
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
  extends UnaryBoundQuery<Tq,Tt>
{
  private static final ClassLogger log=ClassLogger.getInstance(SelectionBinding.class);

  private final Focus<?> paramFocus;
  private Focus<Tt> focus;
  private Channel<Boolean> filter;
  
  public SelectionBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<Tt> store
    )
    throws DataException
  { 
    super(query.getSources(),paramFocus,store);
    setQuery(query);
    this.paramFocus=paramFocus;
    
  }

  public void resolve() throws DataException
  { 
    super.resolve();
    

    focus=new TeleFocus<Tt>(paramFocus,sourceChannel);
    log.fine("Binding constraints "+getQuery().getConstraints());
    try
    { filter=focus.<Boolean>bind(getQuery().getConstraints());
    }
    catch (BindException x)
    { throw new DataException("Error binding constraints "+x,x);
    }
  }
  

  protected SerialCursor<Tt> newSerialCursor(SerialCursor<Tt> source)
  { return new SelectionSerialCursor(source);
  }
  
  protected ScrollableCursor<Tt> newScrollableCursor(ScrollableCursor<Tt> source)
  { return new SelectionScrollableCursor(source);
  }

  class SelectionSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    public SelectionSerialCursor(SerialCursor<Tt> source)
    { super(source);
    }
  
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
//      System.err.println("BoundSelection: eod ");
        return false;
      }
    
      if (filter.get())
      {  
//      System.err.println("BoundSelection: passed "+t);
        dataAvailable(t);
        return true;
      }
      else
      { 
//      System.err.println("BoundSelection: filtered "+t);
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
    { super(source);
    }

    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
//        log.fine("BoundSelection: eod ");
        return false;
      }

      if (filter.get())
      {  
//        log.fine("BoundSelection: passed "+t);
        dataAvailable(t);
        return true;
      }
      else
      { 
//        log.fine("BoundSelection: filtered "+t);
        return false;
      }
    }

    public Type<?> getResultType()
    { return sourceCursor.getResultType();
    }
  }
}