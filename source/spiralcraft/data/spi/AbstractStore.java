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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import spiralcraft.common.LifecycleException;

import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Sequence;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.Schema;
import spiralcraft.data.access.Store;
import spiralcraft.data.core.SequenceField;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;

import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

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
  implements Store
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level debugLevel=ClassLog.getInitialDebugLevel(getClass(),null);

  private boolean started;

  protected final Type<?> sequenceType;  
  protected final EquiJoin sequenceQuery;
  
  protected Schema schema;
  
  private HashSet<Type<?>> authoritativeTypes=new HashSet<Type<?>>();
  
  private LinkedHashMap<Type<?>,Queryable<Tuple>> queryables
    =new LinkedHashMap<Type<?>,Queryable<Tuple>>();

  private HashMap<URI,Sequence> sequences;  
  
  public AbstractStore()
    throws DataException
  {
    sequenceType=Type.resolve("class:/spiralcraft/data/spi/Sequence"); 
      
    sequenceQuery=new EquiJoin();
    sequenceQuery.setSource(new Scan(sequenceType));
    sequenceQuery.setAssignments(".uri=..");
//      sequenceQuery.setDebug(true);
    sequenceQuery.resolve();    
  }
  

  @Override
  public boolean isAuthoritative(Type<?> type)
  { return authoritativeTypes.contains(type);
  }
  
  
  public void setSchema(Schema schema)
  { this.schema=schema;
  }  
  
  
//  @Override
//  public Space getSpace()
//  { return space;
//  }

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
  

  @Override
  public Sequence getSequence(URI uri)
  {
    Sequence sequence=sequences.get(uri);
    return sequence;
  }  

  @Override
  public Type<?>[] getTypes()
  {
    Type<?>[] types=new Type[queryables.size()];
    int i=0;
    for (Queryable<Tuple> queryable: queryables.values())
    { types[i++]=queryable.getTypes()[0];
    }
    return types;
  }
  
  @Override
  public BoundQuery<?,Tuple> query(
    Query query,
    Focus<?> context)
    throws DataException
  { 
    Queryable<Tuple> container=Space.find(context);
    if (container==null)
    { container=this;
    }
    
    assertStarted();
    HashSet<Type<?>> typeSet=new HashSet<Type<?>>();
    query.getScanTypes(typeSet);
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
    
    BoundQuery<?,Tuple> ret=query.solve(context,container);
    ret.resolve();
    if (debugLevel.canLog(Level.DEBUG))
    { log.debug("returning "+ret+" from query("+query+")");
    }
    return ret;

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
  { 
    for (Queryable<?> queryable:queryables.values())
    { addSequences(queryable.getTypes()[0]);
    }
    
    for (Sequence sequence : sequences.values())
    { sequence.start();
    }
    
    started=true;
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    for (Sequence sequence : sequences.values())
    { sequence.stop();
    }
    started=false;
  }  
  
  /**
   * 
   * @param type The Queryable which handles the specified Type
   * @return
   */
  protected Queryable<Tuple> getQueryable(Type<?> type)
  { return queryables.get(type);
  }
   
  protected void addPrimaryQueryable(Type<?> type,Queryable<Tuple> queryable)
  {
    queryables.put(type,queryable);
    addAuthoritativeType(type);
    addBaseTypes(queryable,type);    
  }
  
  
  protected void addAuthoritativeType(Type<?> type)
  { authoritativeTypes.add(type);
  }
  
  
  
  /**
   * <p>Make sure any base-type "union proxies" are set up, to translate a 
   *   Query for the base-type into a union of subtypes.
   * </p>
   * 
   * @param queryable
   */
  protected void addBaseTypes
    (Queryable<Tuple> queryable,Type<?> subtype)
  {
    Type<?> type=subtype.getBaseType();
    while (type!=null)
    { 
      // Set up a queryable for each of the XmlQueryable's base types
      
      Queryable<Tuple> candidateQueryable=getQueryable(type);
      BaseExtentQueryable<Tuple> baseQueryable;
        
      if (candidateQueryable==null)
      { 
        baseQueryable=new BaseExtentQueryable<Tuple>(type);
        addBaseExtentQueryable(type, baseQueryable);
        baseQueryable.addExtent(subtype,queryable);
      }
      else if (!(candidateQueryable instanceof BaseExtentQueryable<?>))
      {
        // The base extent queryable is already "concrete"
        // This is ambiguous, though. The base extent queryable only
        //   contains the non-subtyped concrete instances of the
        //   base type.
          
        baseQueryable=new BaseExtentQueryable<Tuple>(type);
        addBaseExtentQueryable(type, baseQueryable);
        baseQueryable.addExtent(type,candidateQueryable);
        baseQueryable.addExtent(subtype,queryable);
      }
      else
      {
        ((BaseExtentQueryable<Tuple>) candidateQueryable)
          .addExtent(subtype, queryable);
      }
      type=type.getBaseType();
      
    }
    
  }
  
 
  
  /**
   * <p>Called from addBaseTypes to add a queryable for a common base type.
   *   Adds a queryable for the base extent to the set of queryable types.
   * </p>
   * 
   * <p>Override and call super method to perform additional registration
   *   or wrapping of the base type
   * </p>
   * 
   * @param type
   * @param queryable
   */
  protected void addBaseExtentQueryable
    (Type<?> baseType,BaseExtentQueryable<Tuple> queryable)
  { queryables.put(baseType,queryable);
  }
  

  
  /**
   * Create a new Sequence object that manages the sequence 
   *   for the specified field
   * 
   * @param field
   * @return
   */
  protected abstract Sequence createSequence(Field<?> field);

  
  protected void assertStarted()
  { 
    if (!started)
    { throw new IllegalStateException("Store has not been started");
    }
  }
  
  private void addSequences(Type<?> subtype)
  {
    if (subtype.getScheme()!=null)
    {
      if (sequences==null)
      { sequences=new HashMap<URI,Sequence>();
      }
      for (Field<?> field : subtype.getScheme().fieldIterable())
      { 
        if (field instanceof SequenceField<?>)
        { 
          sequences.put
          (field.getURI()
          ,createSequence(field)
          );
          addAuthoritativeType(subtype);
        }
      }
    }
    
  }  
  
}
