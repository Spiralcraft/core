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

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.data.FieldSet;
import spiralcraft.data.DataException;
import spiralcraft.data.Order;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

/**
 * <p>A Query describes a data stream that will retrieve a set of Tuples from a
 *   Queryable, such as a Store or a Space, via a Cursor. 
 * </p>
 * 
 * <p>It is composed of a tree of operations (sub-Queries) that constrain the
 *    set of reachable Tuples to produce a desired result.
 * </p>
 * 
 * <p>A Query must be bound against a Queryable before it can be executed. The
 *    binding process creates the data flow path for the result Tuples.
 * </p>
 *    
 * <p>A Query can be 'solved', or factored into a set of subqueries of lesser 
 *    complexity to allow Queryables to delegate higher level implementation
 *    details back to the system while taking over lower level details. 
 * </p>   
 * 
 * <p>For example, a join may be performed in-process from an out-of-process
 *    source such as a SQL Server and from a text file on disk. The SQL server
 *    would optimize the Select by using a where clause, whereas the text file
 *    would delegate the Select filter back to a default system
 *    implementation.
 * </p>
 */
public abstract class Query
{ 
  protected static final ClassLog log
    =ClassLog.getInstance(Query.class);
  
  /**
   * <p>Return the common base type across the specified Queries,
   *   or null if their types have no commonalities. All Queries must
   *   be resolved and must provide a Type.
   * </p>
   * 
   * @param sources
   * @return
   * @throws DataException
   */
  public static Type<?> commonBaseType(List<Query> queries)
    throws DataException
  {
    Type<?> type=null;
    for (Query query:queries)
    { 
      if (query.getType()==null)
      { 
        throw new 
          DataException("Subquery has no Type: "+query.toString());
      }
      
      if (type==null)
      { type=query.getType();
      }
      else
      { 
        // Make sure all types have something in common, and return
        //   the most concrete common type.
        if (type.hasBaseType(query.getType()))
        { 
          // We found a more general query
          type=query.getType();
        }
        else 
        {
          while (type!=null && !query.getType().hasBaseType(type))
          { 
            type=type.getBaseType();
          }
          
          if (type==null)
          { return null;
          }
        }
      }
      
    }
    return type;
  }
  
  protected final Query baseQuery;
  protected final List<Query> sources=new ArrayList<Query>(1);
  protected Type<?> type;
  protected Level debugLevel
    =ClassLog.getInitialDebugLevel(getClass(),Level.INFO);
  protected boolean logStatistics;
  protected boolean mergeable=false;
  protected Expression<Boolean> conditionX;
  
  /**
   * Construct a new, unfactored Query
   *
   */
  public Query()
  { baseQuery=null;
  }
  
  /**
   * Construct a Query that is the result of factoring the specified downstreamQuery
   *   out of the baseQuery. 
   */
  protected Query(Query baseQuery)
  { this.baseQuery=baseQuery;
  }
  
  /**
   * @return the FieldSet of the Tuples that this query will produce.
   */
  public abstract FieldSet getFieldSet();
  
  /**
   * 
   * @return the Type of the Tuples that this query will produce
   */
  public Type<?> getType()
  { return type;
  }
  
  /**
   * Search for the leaf Types of the Query operation- the full set of Types the
   *   query will access in the persistent store
   * 
   * @param result A set to fill with the results
   * @return The filled result passed in, or if null was passed in, a new Set 
   *   filled with the results
   */
  public Set<Type<?>> getAccessTypes(Set<Type<?>> result)
  { 
    
    if (result==null)
    { result=new HashSet<Type<?>>();
    }
    
    if (this instanceof Scan)
    { result.add(getType());
    }
    
    List<Query> sources=getSources();
    if (sources!=null)
    { 
      for (Query sq : sources)
      { sq.getAccessTypes(result);
      }
    }
    return result;
    
  }
  
  /**
   * <p>Bind the Query to the nearest available Space in the contextual
   *   Focus chain. If no space is available, return the default binding for
   *   this query.
   * </p>
   * 
   * @param context The Focus chain
   * @return A bound query
   * @throws DataException If no Space is resolvable in the Focus chain,
   *   or space.query() throws a DataException
   */
  public BoundQuery<?,?> bind(Focus<?> context)
    throws DataException
  {
    Space space=Space.find(context);
    if (space!=null)
    { return space.query(this, context);
    }
    else
    { 
      BoundQuery<?,?> ret = getDefaultBinding(context,null);
      ret.resolve();
      return ret;
    }
  }

  /**
   * @return The Query that was factored to produce this Query
   */
  public Query getBaseQuery()
  { return baseQuery;
  }
  
  /**
   * @return The upstream sources of this Query
   */
  public List<Query> getSources()
  { return sources;
  }

  /**
   * <p>An optional boolean Expression which ensures that the Query
   *   executes only when the condition returns true.
   * </p>
   * 
   * @param conditionX
   */
  public void setConditionX(Expression<Boolean> conditionX)
  { this.conditionX=conditionX;
  }

  /**
   * Called by the creator/user of the Query to complete any configuration
   *   steps required for the Query to export a FieldSet defining the result.
   */
  public void resolve()
    throws DataException
  { 
    if (sources!=null)
    {
      for (Query source:sources)
      { 
        if (debugLevel.canLog(source.getDebugLevel()))
        { source.setDebugLevel(debugLevel);
        }
        source.resolve();
      }
    }
  }
  
  /**
   * <p>When a Queryable cannot provide an optimized BoundQuery implementation
   *   for a given operation in response to a query(Query) request, this method
   *   will factor the Query by subdividing the Query into downstream and
   *   upstream Queries, represented by a new Query object.
   * </p>
   *   
   * <p>If the query is successfully factored, the new Query will be passed
   *   back to the Queryable for re-evaluation and optimization.
   * </p>
   * 
   * <p>If the query cannot be simplified, the new query will be implemented by
   *   a default implementation, and the upstream sources will be passed back
   *   to the Queryable for evaluation and optimization.
   * </p>
   * 
   * <p>Regardless of which path is chosen, this method will recursively bind
   *   the entire data flow tree for this Query.
   * </p>
   *   
   * 
   * @return Either the default implementation of this Query, or the result of 
   *   Queryable.query() for a factor of this query.
   */
  public final <Ttuple extends Tuple> BoundQuery<?,Ttuple> solve
    (Focus<?> focus,Queryable<Ttuple> store)
    throws DataException
  { 
    Query factor=factor();
    if (factor==null)
    { return getDefaultBinding(focus,store);
    }
    else
    { return store.query(factor, focus);
    }
  }

  /**
   * <p>Create the standard store-independent implementation for this Query 
   *   operation, when a Queryable did not provide a store-optimized 
   *   implementation.
   * </p>
   * 
   * <p>May bind all downstream query results to compute output.
   * </p>
   * 
   * <p>This method is typically called by solve(Focus,Queryable), after
   *   solve() calls factor(). Queries
   *   should not call this directly, because that would short circuit the
   *   optimization process.
   * </p>
   * 
   * <p>May return null if the any of the sources cannot be bound
   * 
   * <p>For complex query chains, this method should call store.query() to bind
   *   all sources.
   * </p>
   */
  abstract <T extends Tuple> BoundQuery<?,T> 
    getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException;
  
  
  /**
   * <p>Select from the output of this query
   * </p>
   * 
   * @param constraints
   * @return
   */
  public Selection select(Expression<Boolean> constraints)
  { return new Selection(this,constraints);
  }

  
  /**
   * <p>Select from the output of this query
   * </p>
   * 
   * @param constraints
   * @return
   */
  public Sort sort(Order order)
  { return new Sort(this,order);
  }

  
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  
  public void setDebug(boolean val)
  { this.debugLevel=val?Level.DEBUG:Level.INFO;
  }
  
  public Level getDebugLevel()
  { return this.debugLevel;
  }
  
  /**
   * Log query runtime and counts
   * 
   * @param val
   */
  public void setLogStatistics(boolean val)
  { this.logStatistics=val;
  }
  
  /**
   * Log query runtime and counts
   * 
   * @return whether statistics should be logged
   */
  public boolean getLogStatistics()
  { return logStatistics;
  }
  
  /**
   * Add a source
   */
  protected void addSource(Query source)
  { sources.add(source);
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
   * <p>If this Query cannot be broken down further, this method must return
   *   null, or an infinite loop will result. The default implementation simply
   *   returns null.
   * </p>
   *   
   * <p>This method is called by solve()
   * </p>
   * 
   * @return The resulting composite Query
   */
  public Query factor()
  { return null;
  }

  /**
   * <p>Indicates whether this type of Query can combine its own results from
   *   multiple sources via the "merge(List<BoundQuery>)" method. 
   * </p>
   * 
   * <p>When false (the default condition), the Query will be "solved()",
   *   otherwise the Query will be bound to multiple sources and the
   *   "merge()" method will be called with a set of BoundQueries, one for
   *   each source.
   * </p>
   *   
   * <p>The default merge operation is a Concatenation. Override merge() to
   *   provide a specific implementation
   * </p>
   * 
   * @return Whether this query and all its sources are mergeable.
   */
  public boolean isMergeable()
  { 
    if (!mergeable)
    { return false;
    }
    
    if (sources!=null)
    {
      for (Query source:sources)
      {
        if (!source.isMergeable())
        { return false;
        }
      }
    }
    return true;
  }
  
  /**
   * <p>Called by the binding chain when mergeable is set to true by a subclass.
   *   Override to provide a specific implementation
   * </p>
   * 
   * @param sources
   * @return
   * @throws DataException
   */
  public <T extends Tuple> BoundQuery<?,T> merge
    (List<BoundQuery<?,T>> sources,Focus<?> paramFocus
    )
    throws DataException
  { return new ConcatenationBinding<Concatenation,T>(sources,null,paramFocus);
  }

}



/*

Concerns:

* A Query is an element of a data flow path of Tuples

* A Query may have multiple children (sources)
     in the case of Joins and Subqueries

* A Query has one or more inputs, a processing component, and a single
     output.
     
* A Query with multiple inputs exposes those inputs as individual source queries
     
* A Query has an output FieldSet, known before it is bound, as a function of
     the input FieldSets the processing FieldSets.

* A Query is bound by a Queryable implementation.
 
* A BoundQuery can be created by the Queryable implementation for each source, and the
    a default BoundQuery can be created from the Query. This grounds the
    implementation. Alternatively, a Queryable can create its own BoundQuery for the
    parent Query, with the sources and the processing dealt with internally to
    the Queryable implementation.
  
* * An Queryable is asked to Bind a Query. 

* * Translation 

* * *  If the Query directly translatable, return a BoundQuery
* * *  Otherwise, break it down
* * * * Break a piece off- DECOMPOSE it by one operation. Creates a Query-Type
          default BoundQuery (grounding)
        The default BoundQuery then binds its simpler source queries.
          
       
       
       
       
*/