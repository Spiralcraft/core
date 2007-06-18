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
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;

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
  { addSource(source);
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public Expression<Boolean> getConstraints()
  { return constraints;
  }
  
  public BoundQuery<?> bind(Focus focus,Queryable store)
    throws DataException
  { return new SelectionBinding<Selection>(this,focus,store);
   
  }
  

    
}

class SelectionBinding<Tq extends Selection>
  extends UnaryBoundQuery<Tq>
{
  private final Focus paramFocus;
  private DefaultFocus<?> focus;
  private Optic<Boolean> filter;
  
  public SelectionBinding
    (Tq query
    ,Focus paramFocus
    ,Queryable store
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
    source.resolve();
    focus=new DefaultFocus<Tuple>(source.getResultBinding());
    focus.setParentFocus(paramFocus);
    try
    { filter=focus.<Boolean>bind(getQuery().getConstraints());
    }
    catch (BindException x)
    { throw new DataException("Error binding constraints "+x,x);
    }
  }
  
  protected boolean integrate(Tuple t)
  { 
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
  
}
