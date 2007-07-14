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
package spiralcraft.data.spi;


import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;

import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.access.Space;
import spiralcraft.data.access.Store;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.BoundQuery;

import spiralcraft.lang.Focus;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

/**
 * A Space that consists of a single Store.
 */
public class SingleSpace<T extends Tuple>
  implements Space<T>
{
  
  private Store<T> store;
  private RegistryNode registryNode;

  public void setStore(Store<T> store)
  { 
    this.store=store;
  }

  public void register(RegistryNode node)
  { 
    registryNode=node;
    node=node.createChild(Space.class,this);
    if (store instanceof Registrant)
    { ((Registrant) store).register(node);
    }
  }
  
  public void initialize()
    throws DataException
  { 
    if (registryNode==null)
    { 
      throw new DataException
        ("SingleSpace: must call register() before initialize()");
    }
    store.initialize();
  }
  
  public BoundQuery<?,?> query(Query query,Focus<?> focus)
    throws DataException
  { 
    // Pass it through
    return store.query(query,focus);
  }

  public boolean containsType(Type<?> type)
  { return store.containsType(type);
  }

  public BoundQuery<?,T> getAll(Type<?> type)
    throws DataException
  { return store.getAll(type);
  }

  public Type<?>[] getTypes()
  { return store.getTypes();
  }
  
  public DataConsumer<DeltaTuple> getUpdater(Type<?> type)
    throws DataException
  { return store.getUpdater(type);
  }
}
