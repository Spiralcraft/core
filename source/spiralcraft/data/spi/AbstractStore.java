//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.spi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;

import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.Store;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;
// import spiralcraft.data.query.Scan;

import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

/**
 * <p>Starting point for building a new type of Store.
 * </p>
 * 
 * <p>The AbstractStore generally represents a set of Queryables that provide
 *   access to data for a set of Types. 
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractStore
  implements Store,Registrant,Lifecycle
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level debugLevel=ClassLog.getInitialDebugLevel(getClass(),null);

  protected RegistryNode registryNode;
  protected Space space;
  private boolean started;
  
  
  public void register(RegistryNode node)
  { 
    this.space=node.findInstance(Space.class);
    registryNode=node.createChild(getClass(),this);
  }
  
  @Override
  public Space getSpace()
  { return space;
  }

  @Override
  public boolean containsType(
    Type<?> type)
  { 
    assertStarted();
    return getQueryable(type)!=null;
  }
  
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  
  /**
   * 
   * @param type The Queryable which handles the specified Type
   * @return
   */
  protected abstract Queryable<Tuple> getQueryable(Type<?> type);
  
  protected void getScanTypes(Query q,Set<Type<?>> result)
  { 
   
    
    if (q instanceof Scan)
    { result.add(q.getType());
    }
    
    List<Query> sources=q.getSources();
    if (sources!=null)
    { 
      for (Query sq : sources)
      { getScanTypes(sq,result);
      }
    }
    
  }
  
  @Override
  public BoundQuery<?,Tuple> query(
    Query query,
    Focus<?> context)
    throws DataException
  { 
    assertStarted();
    HashSet<Type<?>> typeSet=new HashSet<Type<?>>();
    getScanTypes(query,typeSet);
    Type<?>[] types=typeSet.toArray(new Type[typeSet.size()]);
    
    if (types.length==1)
    {
      Queryable<Tuple> queryable=getQueryable(types[0]);
      if (queryable==null)
      { return null;
      }
      else
      { return queryable.query(query, context);
      }
    }
    
    BoundQuery<?,Tuple> ret=query.solve(context,this);
    ret.resolve();
    if (debugLevel.canLog(Level.DEBUG))
    { log.debug("returning "+ret+" from query("+query+")");
    }
    return ret;

//    if (query instanceof Scan)
//    { 
//      // Basic scan just calls getAll
//      return getAll(query.getType());
//    }
//    else if 
//      (query.getSources()!=null 
//      && query.getSources().size()==1
//      && query.getSources().get(0) instanceof Scan
//      )
//    { 
//      // Query derived from Scan goes to Queryable for optimization
//      
//      Type<?> queryType=query.getSources().get(0).getType();
//      Queryable<Tuple> queryable=getQueryable(queryType);
//      if (queryable==null)
//      { throw new DataException
//          ("This store cannot query type "+queryType.getURI());
//      }
//      return queryable.query(query, context);
//    }
//    else
//    { 
//      // Solve it until we get something we can understand
//      return query.solve(context,getSpace());
//    }    

  }
  
  @Override
  public BoundQuery<?,Tuple> getAll(
    Type<?> type)
    throws DataException
  {
    assertStarted();
    
    Queryable<Tuple> queryable=getQueryable(type);
    if (queryable!=null)
    { return queryable.getAll(type);
    }
    return null;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { started=true;
  }

  @Override
  public void stop()
    throws LifecycleException
  { started=false;
  }  
  
  protected void assertStarted()
  { 
    if (!started)
    { throw new IllegalStateException("Store has not been started");
    }
  }
}
