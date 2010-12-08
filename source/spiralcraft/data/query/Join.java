//
// Copyright (c) 1998,2010 Michael Toth
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


import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;

/**
 * <p>A Query operation which uses each returned element of a parent query as the 
 *   context for a child Query and returns the concatenated result of the child
 *   query.
 * </p>
 */
public class Join
  extends Query
{
 
  private static final ClassLog log
    =ClassLog.getInstance(Selection.class);
  private Query parentQuery;
  private Query childQuery;
  
  { mergeable=true;
  }
  
  public Join()
  {
  }
  
  /**
   * Construct a Join which reads data from the specified parent Query and 
   *   concatenates the results of the child Query for each result of
   *   the parent.
   */
  public Join(Query parentQuery,Query childQuery)
  { 
    addSource(parentQuery);
    this.childQuery=childQuery;
  }
  
  @Override
  public FieldSet getFieldSet()
  { return childQuery.getFieldSet();
  }
    
  public Join
      (Join baseQuery
      ,Query childQuery
      )
  { 
    super(baseQuery);
    this.childQuery=childQuery;
  }

  
  /**
   * Specify the child query to be run in the context of each Tuple returned
   *   by the parent query
   */
  public void setChildQuery(Query childQuery)
  { 
    type=childQuery.getType();
    this.childQuery=childQuery;
  }
 
  public void setParentQuery(Query parentQuery)
  { 
    if (this.parentQuery!=null)
    { this.sources.clear();
    }
    this.parentQuery=parentQuery;
    addSource(parentQuery);
  }
  
  public Query getParentQuery()
  { return parentQuery;
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public Query getChildQuery()
  { return childQuery;
  }
  

  
  @Override
  public <T extends Tuple> BoundQuery<?,T> 
    getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new JoinBinding<Join,T>(this,focus,store);
   
  }
  
  
  /**
   * <p>Factor a base Query into a downstream Query (returned) and 
   *   one or more upstream Queries (from getSources()) that achieve the same
   *   result as this Query. This method is used to provide Queryables with 
   *   fine control when implementing optimized versions of the various Query
   *   operations.
   * </p>
   *   
   * <p>The upstream Queries implement the bulk of the operations in the
   *   original Query, while the downstream Query implements a single operation 
   *   factored out of the base Query.
   * </p>
   * 
   * 
   * @return The downstream Query resulting from factoring this Query, or null if
   *   the Query cannot be factored.
   */
  @Override
  protected Query factor()
  { 
    if (debugLevel.canLog(Level.DEBUG))
    { log.debug("factor()");
    }   
    
    return null;
  }
  
  @Override
  public String toString()
  { return super.toString()
      +"[childQuery = "+childQuery+"]: sources="
      +getSources().toString();
  }

  
}

class JoinBinding<Tq extends Join,Tt extends Tuple>
  extends BoundQuery<Tq,Tt>
{
  private final Focus<?> paramFocus;
  private Focus<Tuple> parentFocus;
  private boolean resolved;
  private BoundQuery<?,Tt> childBinding;
  private BoundQuery<?,Tuple> parentBinding;
  
  private ThreadLocalChannel<Tuple> parentLocal;
  
  public JoinBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super();
    setQuery(query);
    this.paramFocus=paramFocus;
    
  }

  @SuppressWarnings("unchecked")
  @Override
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
      
      parentBinding
        =(BoundQuery<?,Tuple>) getQuery().getParentQuery().bind(paramFocus);
      
      Query childQuery=getQuery().getChildQuery();
      
      try
      {
        parentLocal
          =new ThreadLocalChannel<Tuple>
            (DataReflector.<Tuple>getInstance(parentBinding.getType()));
      }
      catch (BindException x)
      { throw new DataException("Error binding child query context",x);
      }
      
      parentFocus= paramFocus.chain(parentLocal);
      
      if (debugLevel.canLog(Level.DEBUG))
      { log.debug("Binding child query "+childQuery);
      }
      
      childBinding=(BoundQuery<?,Tt>) childQuery.bind(parentFocus);
      if (debugLevel.canLog(Level.FINE))
      { childBinding.setDebugLevel(debugLevel);
      }
      resolved=true;
    }
  }
  

  @Override
  public SerialCursor<Tt> execute()
    throws DataException
  { return new JoinSerialCursor();
  }

  protected class JoinSerialCursor
    extends BoundQuerySerialCursor
  {
    
    private final SerialCursor<Tuple> parentCursor;
    private SerialCursor<Tt> childCursor;
    
    public JoinSerialCursor()
      throws DataException
    { 
      super();
      parentCursor=parentBinding.execute();
    }
  

    @SuppressWarnings("unchecked")
    @Override
    public boolean next()
      throws DataException
    {
      parentLocal.push(parentCursor.getTuple());
      try
      {
        
        while (true)
        { 
          if (childCursor!=null)
          {
            if (childCursor.next())
            { 
              dataAvailable((Tt) childCursor.getTuple().snapshot());
              return true;
            }
            else
            { 
              childCursor.close();
              childCursor=null;
            }
          }
          
          if (childCursor==null)
          { 
            
            if (parentCursor.next())
            {
              parentLocal.set(parentCursor.getTuple());
              childCursor=childBinding.execute();
            }
            else
            { return false;
            }
            
          }

        }
      }
      finally
      { parentLocal.pop();
      }
    }
    
    @Override
    public void close()
      throws DataException
    {
      if (childCursor!=null)
      { 
        childCursor.close();
        childCursor=null;
      }
      if (parentCursor!=null)
      { parentCursor.close();
      }
    }
    
    
    
    @Override
    public Type<?> getResultType()
    { 
      Type<?> ret=childBinding.getType();
      if (ret!=null)
      { return ret;
      }
      else
      { 
        log.fine("Child binding type is null "+childBinding);
        return null;
      }
    }

  }

}