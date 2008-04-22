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

import java.util.List;
import java.util.ArrayList;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

import spiralcraft.data.FieldSet;
import spiralcraft.data.DataException;
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
  protected final Query baseQuery;
  protected final List<Query> sources=new ArrayList<Query>(1);
  protected Type<?> type;
  protected boolean debug;
  
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
  
  public Type<?> getType()
  { return type;
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
        if (debug)
        { source.setDebug(true);
        }
        source.resolve();
      }
    }
  }
  
  /**
   * <P>When a Queryable cannot provide an optimized BoundQuery implementation for a given
   *   operation in response to a query(Query) request, this method will factor the
   *   Query by subdividing the Query into downstream and upstream Queries. 
   *   
   * <P>The downstream Query (closer to the caller) will be implemented by a 
   *   default implementation that receives data from upstream component(s).
   *   
   * <P>One or more upstream Queries will be passed back through the query(Query request),
   *   providing the Queryable with an opportunity to optimize another level of the data
   *   flow tree.
   * 
   * @return the standard BoundQuery for the downstream operation of this Query,
   *   bound to one or more sub-Query operations which will be bound against the
   *   Queryable store. This will recursively bind the entire dataflow tree underneath this 
   *   query and return the root BoundQuery.
   */
  public BoundQuery<?,?> solve(Focus<?> focus,Queryable<?> store)
    throws DataException
  { 
    Query factor=factor();
    if (factor.getSources().size()==0)
    { return null;
    }

    return factor.getDefaultBinding(focus,store);
  }

  /**
   * <p>Create the standard store-independent implementation for this Query 
   *   operation, when a Queryable did not provide a store-optimized 
   *   implementation
   * </p>
   * 
   * <p>May retrieve all downstream query results to compute output.
   * </p>
   */
  public abstract <T extends Tuple> BoundQuery<?,T> 
    getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException;
  
  
  public Selection select(Expression<Boolean> constraints)
  { return new Selection(this,constraints);
  }
  
  public void setDebug(boolean val)
  { this.debug=val;
  }
  
  public boolean getDebug()
  { return debug;
  }
  
  /**
   * Add a source
   */
  protected void addSource(Query source)
  { sources.add(source);
  }

  
  /**
   * <P>Factor a base Query into a downstream Query (returned) and 
   *   one or more upstream Queries (from getSources()) that achieve the same result
   *   as this Query. This method is used to provide Queryables with fine control
   *   when implementing optimized versions of the various Query operations.
   *   
   * <P>The upstream Queries implement the bulk of the operations in the
   *   original Query, while the downstream Query implements a single operation 
   *   factored out of the base Query.
   * 
   * <P>The default implementation simple returns this Query as the downstream Query,
   *   and this Query's sources as the upstream Queries.
   *   
   * <P>Overridden implementations will normally take a more granular approach to
   *   factoring.
   * 
   * @return The upstream Queries resulting from factoring this Query, or null if
   *   the Query cannot be factored.
   */
  protected Query factor()
  { return this;
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