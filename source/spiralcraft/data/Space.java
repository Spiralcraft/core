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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import spiralcraft.app.Component;
import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.StandardContainer;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;

import spiralcraft.data.access.Store;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Concatenation;
import spiralcraft.data.query.ConcatenationBinding;
import spiralcraft.data.session.DataSession;

import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
//import spiralcraft.lang.util.LangUtil;

import spiralcraft.log.ClassLog;

import spiralcraft.service.Service;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.ListMap;
import spiralcraft.util.refpool.URIPool;


/**
 * <p>A logical data container. Provides access to a complete set of data used 
 *   by one or more applications. Data reachable from a Space may be
 *   contained in or more Stores. 
 * </p>
 *   
 */
public class Space
  extends AbstractComponent
  implements Queryable<Tuple>,Service
{
  public static final URI SPACE_URI 
    = URIPool.create("class:/spiralcraft/data/Space");
  public static final ClassLog log
    =ClassLog.getInstance(Space.class);
  
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
  

  // private Space parent;
  private final HashMap<URI,Sequence> globalSequences
    =new HashMap<URI,Sequence>();
  
  private Store[] stores=new Store[0];
  
  private Type<?>[] types;
  
  private ListMap<Type<?>,Store> typeStores
    =new ListMap<Type<?>,Store>();

  private HashMap<String,Store> storeMap
    =new HashMap<String,Store>();
  
  private ThreadLocalChannel<DataSession> dataSessionChannel;
  
  public void setStores(Store[] stores)
  { 
    this.stores=stores;
  }

  public Store getStore(String storeName)
  { return storeMap.get(storeName);
  }
  
  /**
   * The Services that will run in the context of this Space
   * 
   * @param services
   */
  public void setServices(final Service[] services)
  {
    this.childContainer
      =new StandardContainer(this)
    {
      { 
        children=new Component[services.length];
        int i=0;
        for (Component service:services)
        { children[i++]=service;
        }
      }
    };
  }
  
  
  @Override
  public Type<?>[] getTypes()
  { 

    return types;
  }
  
  
  public void addStore(Store store)
  { 
    if (this.bound)
    { throw new IllegalStateException("Cannot add a store to a bound Space");
    }
    stores=ArrayUtil.append(stores,store);
  }
 
  public void exportSequence(URI uri,Sequence sequence)
  { globalSequences.put(uri,sequence);
  }
  
  @Override
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
        return new ConcatenationBinding<Concatenation,Tuple>(boundQueries,type,selfFocus);
      }
    }
//    else if (parent!=null)
//    { return parent.getAll(type);
//    }
    return null;
  }


  @Override
  public Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  { 
    
    // parent=LangUtil.findInstance(Space.class,focusChain);
    dataSessionChannel
      =new ThreadLocalChannel<DataSession>
        (BeanReflector.<DataSession>getInstance(DataSession.class)
        ,true
        );
    
    Focus<?> storeFocus=selfFocus.chain(selfFocus.getSubject());
    
    // Add a reference to a data session visible only to stores for 
    //   internal binding of data ops.
    storeFocus.addFacet
      (new SimpleFocus<DataSession>(selfFocus,dataSessionChannel));
    for (Store store: stores)
    { 
      store.bind(storeFocus);
      storeMap.put(store.getName(),store);      
    }
    computeTypes();
    return focusChain;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    Lifecycler.start(stores);
    super.start();
  }
  
  
  private void computeTypes()
  {
    LinkedHashSet<Type<?>> set=new LinkedHashSet<Type<?>>();
    for (Store store:stores)
    { 
      if (store.isPublic())
      {
        for (Type<?> type:store.getTypes())
        { 
          set.add(type);
          typeStores.add(type,store);
        }
      }
    }
    types=set.toArray(new Type<?>[set.size()]);
    
  }
  
  @Override
  public void stop()
    throws LifecycleException
  { 
    super.stop();
    Lifecycler.stop(stores);
  }
  
  
  @Override
  public BoundQuery<?,Tuple> query(Query query,Focus<?> focus)
    throws DataException
  {
    BoundQuery<?,Tuple> ret=solve(query,focus);
    
    if (ret==null)
    { 
      ret=query.solve(focus,this);
      ret.resolve();
    }
    return ret;
  }
  
  @Override
  public BoundQuery<?,Tuple> solve(Query query,Focus<?> focus)
    throws DataException
  { 
    
    List<BoundQuery<?,Tuple>> queries
      =new LinkedList<BoundQuery<?,Tuple>>();
    
    Set<Type<?>> scanTypes=query.getAccessTypes(null);

    if (scanTypes.size()==0)
    { return null;
    }
    
    
    Set<Store> stores=new HashSet<Store>();
    for (Type<?> type: scanTypes)
    { 
      List<Store> typeStoreList=typeStores.get(type);
      if (typeStoreList!=null)
      { stores.addAll(typeStores.get(type));
      }
      else
      { 
        throw new DataException
          ("Type "+type.getURI()+" is not stored in this Space");
      }
    }
    
    boolean mergeable=query.isMergeable();
    for (Store store: stores)
    {
      // Stores that can't process the type should return null
      BoundQuery<?,Tuple> boundQuery=store.solve(query,focus);
      if (boundQuery!=null)
      { 
        if (queries.size()>0 && !mergeable)
        { return null;
        }
        queries.add(boundQuery);
      }
    }

    if (queries.isEmpty())
    { return null;
    }
    else if (queries.size()==1)
    { return queries.get(0);
    }
    else
    { 
      // The Query needs to solve for multiple sources.
      return query.merge(queries,focus);
      
    }
  
  
  }

  @Override
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
    getUpdater(Type<?> type)
    throws DataException
  {
    Store store=getAuthoritativeStore(type);
    
    if (store!=null)
    { return store.getUpdater(type);
    }
    else
    { 
      if (containsType(type))
      {
        
        throw new DataException
          ("Could not find authoritative store for type "+type.getURI());
      }
      else
      {
        throw new DataException
          ("Type not contained in this space "+type.getURI());
      }
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
        if (store.isAuthoritative(type))
        { return store;
        }
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
    Sequence sequence=globalSequences.get(uri);
    if (sequence!=null)
    { return sequence;
    }
    
    try
    { 
      // Strip fragment, which is field name
      URI typeURI
        =URIPool.get
          (new URI
            (uri.getScheme()
            ,uri.getAuthority()
            ,uri.getPath()
            ,uri.getQuery()
            ,null)
          );
      //
      // We may have to explicitly mark something as being an authoritative
      //   store for an abstract base type with subtypes in multiple stores
      // 
      // XXX: The default is to get the sequence from store being updated,
      //   so this method is only used to resolve a "global" sequence
      //   which is not yet fully implemented anyway
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
  
  public void pushDataSession(DataSession session)
  { dataSessionChannel.push(session);
  }
  
  public void popDataSession()
  { dataSessionChannel.pop();
  }
  
}
