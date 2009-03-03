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


import java.net.URI;
import java.util.Date;

import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.Sequence;
import spiralcraft.data.Space;
import spiralcraft.data.Type;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;

import spiralcraft.data.access.Store;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.BoundQuery;

import spiralcraft.lang.Focus;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;


/**
 * A Space that consists of a single Store.
 */
public class SingleSpace
  extends Space
  implements Registrant
{
  
  private Store store;
  private RegistryNode registryNode;

  public void setStore(Store store)
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
  
  public void start()
    throws LifecycleException
  { 
    if (registryNode==null)
    { 
      throw new LifecycleException
        ("SingleSpace: must call register() before start()");
    }
    if (store!=null)
    { store.start();
    }
    else
    { throw new LifecycleException("Store is null");
    }
  }
  
  public void stop()
    throws LifecycleException
  { store.stop();
  }
  
  public BoundQuery<?,Tuple> query(Query query,Focus<?> focus)
    throws DataException
  { 
    // Pass it through
    BoundQuery<?,Tuple> boundQuery
      =store.query(query,focus);
    if (boundQuery==null)
    { throw new DataException("No path to process Query "+query);
    }
    return boundQuery;
    
    
  }

  public boolean containsType(Type<?> type)
  { return store.containsType(type);
  }

  public BoundQuery<?,Tuple> getAll(Type<?> type)
    throws DataException
  { return store.getAll(type);
  }

  public Type<?>[] getTypes()
  { return store.getTypes();
  }
  
  @Override
  public DataConsumer<DeltaTuple> getUpdater(Type<?> type,Focus<?> focus)
    throws DataException
  { return store.getUpdater(type,focus);
  }
  
  @Override
  public Sequence getSequence(URI uri)
    throws DataException
  { return store.getSequence(uri);
  }
  
  @Override
  public Date getNowTime()
  { return new Date();
  }
  
}
