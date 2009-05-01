//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.common.LifecycleException;

import spiralcraft.data.access.Store;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Concatenation;
import spiralcraft.data.query.ConcatenationBinding;

import spiralcraft.lang.Focus;
import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;
import spiralcraft.service.Service;
import spiralcraft.util.ListMap;


/**
 * <p>A logical data container. Provides access to a complete set of data used by
 *   one or more applications. Data reachable from a Space may be
 *   contained in or more Stores. 
 * </p>
 *   
 */
public class Space
  implements Queryable<Tuple>,Service
{
  public static final URI SPACE_URI 
    = URI.create("class:/spiralcraft/data/Space");
  
  public static final Space find(Focus<?> focus)
  {
    Focus<Space> spaceFocus=focus.<Space>findFocus(SPACE_URI);
    if (spaceFocus==null)
    { return null;
    }
    else
    { return spaceFocus.getSubject().get();
    }
  }

  private Store[] stores;
  private RegistryNode registryNode;
  
  private Type<?>[] types;
  
  private ListMap<Type<?>,Store> typeStores
    =new ListMap<Type<?>,Store>();
  
  public void setStores(Store[] stores)
  { 
    this.stores=stores;
  }

  public Type<?>[] getTypes()
  { 

    return types;
  }
  
  @Override
  public void register(RegistryNode node)
  { 

    registryNode=node;
    node=node.createChild(Space.class,this);
    for (Store store: stores)
    { 
      if (store instanceof Registrant)
      { ((Registrant) store).register(node);
      }
    }
  }
  
  public BoundQuery<?,Tuple> getAll(Type<?> type)
    throws DataException
  { 
    List<Store> stores=typeStores.get(type);
    if (stores!=null)
    {
      if (stores.size()==0)
      { return null;
      }
      else if (stores.size()==1)
      { return stores.get(0).getAll(type);
      }
      else
      { 
        List<BoundQuery<?,Tuple>> boundQueries
          =new LinkedList<BoundQuery<?,Tuple>>();
        for (Store store: stores)
        { boundQueries.add(store.getAll(type));
        }
        return new ConcatenationBinding<Concatenation,Tuple>(boundQueries);
      }
    }
    return null;
  }


  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (registryNode==null)
    { 
      throw new LifecycleException
        ("SingleSpace: must call register() before start()");
    }
    
    for (Store store: stores)
    { store.start();
    }
    
    computeTypes();
  }
  
  
  private void computeTypes()
  {
    LinkedHashSet<Type<?>> set=new LinkedHashSet<Type<?>>();
    for (Store store:stores)
    { 
      for (Type<?> type:store.getTypes())
      { 
        set.add(type);
        typeStores.add(type,store);
      }
    }
    types=set.toArray(new Type[set.size()]);
    
  }
  
  @Override
  public void stop()
    throws LifecycleException
  { 
    for (Store store:stores)
    { store.stop();
    }
  }
  
  public BoundQuery<?,Tuple> query(Query query,Focus<?> focus)
    throws DataException
  { 
    
    List<BoundQuery<?,Tuple>> queries
      =new LinkedList<BoundQuery<?,Tuple>>();
    
    for (Store store: stores)
    {
      // Pass it through
      BoundQuery<?,Tuple> boundQuery=store.query(query,focus);
      if (boundQuery!=null)
      { queries.add(boundQuery);
      }
    }

    if (queries.isEmpty())
    { throw new DataException("No path to process Query "+query);
    }
    else if (queries.size()==1)
    { return queries.get(0);
    }
    else
    { return new ConcatenationBinding<Concatenation,Tuple>(queries);
    }
  
  
  }

  public boolean containsType(Type<?> type)
  { return typeStores.containsKey(type);
  }

  /**
   * <p>Retrieve an update 'channel'. The DataConsumer can be used once to update
   *   a batch of Tuples of the same Type.
   * </p>
   * 
   * <p>Expressions contained in Fields may reference components available
   *   from the provided Focus to provide default values, sequences, timestamps,
   *   etc. available through the DataSession.
   * </p>
   * 
   * @return A DataConsumer which is used to push one or more updates into
   *   this Space. 
   */
  public DataConsumer<DeltaTuple> 
    getUpdater(Type<?> type,Focus<?> focus)
    throws DataException
  {
    Store store=getAuthoritativeStore(type);
    
    if (store!=null)
    { return store.getUpdater(type,focus);
    }
    else
    { 
      throw new DataException
        ("Could not find authoritative store for type "+type.getURI());
    }    
  }
  
  /**
   * 
   * @param type
   * @return
   */
  private Store getAuthoritativeStore(Type<?> type)
  { 
    List<Store> stores=typeStores.get(type);
    if (stores!=null)
    {
      for (Store store: stores)
      {
        // XXX Need to add to store interface to determine if
        //  authoritative for type
        return store;
      }
    }
    return null;
  }
  
  /**
   * <p>Return a Sequence for generating primary key data, or null if
   *   sequential ids are not provided for the specified URI. The URI is
   *   usually that of a specific Field (ie. Type.getURI()+"#"+field.getName())
   *   that denotes a primary key.  
   * </p>
   * 
   * @param type
   * @return
   * @throws DataException
   */
  public Sequence getSequence(URI uri)
    throws DataException
  {
    try
    { 
      
      URI typeURI=new URI(uri.getScheme(),uri.getFragment(),null);
      Store store=getAuthoritativeStore(Type.resolve(typeURI));
      
      if (store!=null)
      { return store.getSequence(uri);
      }
      else
      { 
        throw new DataException
          ("Could not find authoritative store for type "+typeURI);
      }
    }
    catch (URISyntaxException x)
    { throw new DataException("URI Syntax error in '"+uri+"'",x);
    }
  }
  
  /**
   * A Space provides a time reference, which should be used by all application
   *   components which integrate time into data. The nowTime for a Space
   *   is usually the current time, but may vary in situations where it
   *   is fixed or frozen for testing, development, debugging or other 
   *   special purposes.
   * 
   * @return The current time according to the Space
   */ 
  public Date getNowTime()
  { return new Date();
  }

}
