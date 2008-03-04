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
import spiralcraft.lang.ParseException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;

import java.util.ArrayList;

/**
 * A Query operation which specifies constrains the result of another Query
 */
public class EquiJoin
  extends Query
{
  private ArrayList<Expression<?>> expressions;
  private ArrayList<String> names;
  private String[] assignments;
  
  public EquiJoin()
  { 
  }
  

  
  /**
   * <p>Construct an Equijoin, which queries rows by specifying the values of
   *   a set of fields that define a relation.
   * </p>
   * 
   * <p>An EquiJoin permits the use of a set of field values as a hashable key
   *   to identify a result.
   * </p> 
   *   
   */
  public EquiJoin(Query source,String[] assignments)
  { 
    setAssignments(assignments);
    addSource(source);
  }
  
  public void setAssignments(String[] assignments)
    throws IllegalArgumentException
  {
  
    for (String assignment : assignments)
    { 
      int eqPos=assignment.indexOf('=');
      if (assignment.length()==eqPos+1
            || assignment.charAt(eqPos+1)=='='
         )
      { 
        throw new IllegalArgumentException
          ("Expression not of the form 'x=y...': '"+assignment+"'");
      }
      
      names.add(assignment.substring(0,eqPos));
      try
      { expressions.add(Expression.parse(assignment.substring(eqPos+1)));
      }
      catch (ParseException x)
      { throw new IllegalArgumentException(x);
      }
    }
    
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
    
  public EquiJoin
      (Selection baseQuery
      ,Expression<Boolean> constraints
      )
  { 
    super(baseQuery);
  }

 
  public void setSource(Query source)
  { 
    type=source.getType();
    addSource(source);
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public String[] getAssignments()
  { return assignments;
  }
  
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<T> store)
    throws DataException
  { return new EquiJoinBinding<EquiJoin,T>(this,focus,store);
   
  }
  
  public ArrayList<String> getNames()
  { return names;
  }
  
  public ArrayList<Expression<?>> getExpressions()
  { return expressions;
  }
    
}

class EquiJoinBinding<Tq extends EquiJoin,Tt extends Tuple>
  extends UnaryBoundQuery<Tq,Tt>
{
  private final Focus<?> paramFocus;
  private SimpleFocus<?> focus;
  private Channel<Boolean>[] filter;
  
  public EquiJoinBinding
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

  @SuppressWarnings("unchecked")
  public void resolve() throws DataException
  { 
    super.resolve();
    source.resolve();
    focus=new SimpleFocus<Tt>(sourceChannel);
    focus.setParentFocus(paramFocus);
    try
    { 
      int i=0;
      ArrayList<Expression<?>> expressions=getQuery().getExpressions();
      filter=new Channel[expressions.size()];
      for (String name : getQuery().getNames() )
      { 
        try
        {
          Expression<Boolean> expression
            =new Expression<Boolean>
              (Expression.parse(name).getRootNode()
                .isEqual(expressions.get(i).getRootNode())
                ,name+"=="+expressions.get(i).getText()
              );

          filter[i++]=focus.<Boolean>bind(expression);
        }
        catch (ParseException x)
        { 
          throw new DataException
            ("Error parsing equijoin expression ",x);
        }
        
      }
      
    }
    catch (BindException x)
    { throw new DataException("Error binding constraints "+x,x);
    }
  }
  
  
  protected SerialCursor<Tt> newSerialCursor(SerialCursor<Tt> source)
  { return new EquiJoinSerialCursor(source);
  }
  
  protected ScrollableCursor<Tt> newScrollableCursor(ScrollableCursor<Tt> source)
  { return new EquiJoinScrollableCursor(source);
  }

  class EquiJoinSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    public EquiJoinSerialCursor(SerialCursor<Tt> source)
    { super(source);
    }
  
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
//        System.err.println("BoundEquiJoin: eod ");
        return false;
      }
      
      boolean result=true;
      for (Channel<Boolean> element: filter)
      { 
        if (!element.get())
        { 
          result=false;
          break;
        }
      }
       
      if (result)
      { 
//        System.err.println("BoundEquiJoin: passed "+t);
        dataAvailable(t);
        return true;
      }
      else
      { 
//        System.err.println("BoundEquiJoin: filtered "+t);
        return false;
      }
    }
  }

  class EquiJoinScrollableCursor
    extends UnaryBoundQueryScrollableCursor
  {
    public EquiJoinScrollableCursor(ScrollableCursor<Tt> source)
    { super(source);
    }

    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
//        System.err.println("BoundEquiJoin: eod ");
        return false;
      }
      
      boolean result=true;
      for (Channel<Boolean> element: filter)
      { 
        if (!element.get())
        { 
          result=false;
          break;
        }
      }
       
      if (result)
      { 
//        System.err.println("BoundEquiJoin: passed "+t);
        dataAvailable(t);
        return true;
      }
      else
      { 
//        System.err.println("BoundEquiJoin: filtered "+t);
        return false;
      }
    }
  }
  
  
}
