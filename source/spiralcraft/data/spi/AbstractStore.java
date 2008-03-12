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

import spiralcraft.builder.Lifecycle;
import spiralcraft.builder.LifecycleException;

import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.Store;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;

import spiralcraft.lang.Focus;
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
  protected RegistryNode registryNode;
  protected Space space;
  
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
  { return getQueryable(type)!=null;
  }
  
  /**
   * 
   * @param type The Queryable which handles the specified Type
   * @return
   */
  protected abstract Queryable<Tuple> getQueryable(Type<?> type);
  
  @Override
  public BoundQuery<?,?> query(
    Query query,
    Focus<?> context)
    throws DataException
  {
    if (query instanceof Scan)
    { return getAll(query.getType());
    }
    else
    { 
      // Solve it until we get something we can understand
      return query.solve(context,getSpace());
    }    

  }
  
  @Override
  public BoundQuery<?,Tuple> getAll(
    Type<?> type)
    throws DataException
  {
    Queryable<Tuple> queryable=getQueryable(type);
    if (queryable!=null)
    { return queryable.getAll(type);
    }
    return null;
  }
  
  @Override
  public void start()
    throws LifecycleException
  {

  }

  @Override
  public void stop()
    throws LifecycleException
  {
    // TODO Auto-generated method stub
    
  }  
  
  
}
