//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.data.util;


import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Provides a bi-directional mapping between and upstream and a downstream
 *   list of values via a relational Query. Each side of the mapping is
 *   associated with a candidate key of the relation.
 * </p>
 * 
 * @author mike
 *
 * @param <Tchannel>
 * @param <Tsource>
 */
public class RelationalMap<Tdownstream,TdownstreamItem,Tupstream,TupstreamItem>
  implements ChannelFactory<Tdownstream, Tupstream>,Contextual
{
  private static final ClassLog log
    =ClassLog.getInstance(RelationalMap.class);
  
  private Type<?> entityType;
  private String upstreamFieldName;
  private String downstreamFieldName;
  private Query downstreamQuery;
  private Expression<TdownstreamItem> downstreamProjectionX;
  private Query upstreamQuery;
  private Expression<TupstreamItem> upstreamProjection;
  private Queryable<Tuple> queryable;
  
  private Focus<Tuple> resultFocus;
  private ThreadLocalChannel<Tuple> resultChannel;
  private Channel<TdownstreamItem> downstreamProjection;  

  private Level debugLevel=Level.INFO;
  
  public RelationalMap()
  {
  }
  
  public void setDebug(boolean debug)
  { 
    if (debug)
    { this.debugLevel=Level.FINE;
    }
    else
    { this.debugLevel=Level.INFO;
    }
  }
  
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  
  public RelationalMap
    (Type<Tuple> entityType
    ,String upstreamFieldName
    ,String downstreamFieldName
    )
  { 
    this.entityType=entityType;
    this.upstreamFieldName=upstreamFieldName;
    this.downstreamFieldName=downstreamFieldName;
  }

  public void setType(Type<?> entityType)
  { this.entityType=entityType;
  }
  
  public void setUpstreamFieldName(String fieldName)
  { this.upstreamFieldName=fieldName;
  }

  public void setDownstreamFieldName(String fieldName)
  { this.downstreamFieldName=fieldName;
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    queryable=Space.find(focusChain);
    try
    {
      downstreamQuery
        =new EquiJoin
          (entityType
          ,Expression.create("."+upstreamFieldName)
          ,Expression.create("..")
          );
      downstreamQuery.setDebug(true);
      downstreamQuery.resolve();
      downstreamProjectionX=Expression.create("."+downstreamFieldName);
    
      upstreamQuery
      =new EquiJoin
        (entityType
          ,Expression.create("."+downstreamFieldName)
          ,Expression.create("..")
        );
      upstreamQuery.resolve();
      upstreamProjection=Expression.create("."+upstreamFieldName);
      
      resultChannel=new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(entityType));
      resultFocus=focusChain.chain(resultChannel);

      downstreamProjection
        =resultFocus.<TdownstreamItem>bind(downstreamProjectionX);      
      
    }
    catch (DataException x)
    { throw new BindException("Error resolving query",x);
    }
    return focusChain;
  }
  
  
  @SuppressWarnings("unchecked")
  @Override
  public Channel<Tdownstream> bindChannel(
    Channel<Tupstream> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    // TODO Auto-generated method stub
    return new RelationalMapChannel
      ((Reflector<Tdownstream>) 
          ArrayReflector.getInstance(downstreamProjection.getReflector())
      ,source
      ,focus
      );
  }

  class RelationalMapChannel
    extends SourcedChannel<Tupstream,Tdownstream>
  {
    private CollectionDecorator<Tupstream,TupstreamItem> sourceCollection;

    private final ThreadLocalChannel<Tdownstream> downstreamSource;
    private CollectionDecorator<Tdownstream,TdownstreamItem> downstreamSourceCollection;
    
    
    private final ThreadLocalChannel<TupstreamItem> upstreamInputItem;
    private final ThreadLocalChannel<TdownstreamItem> downstreamInputItem;
    

    
    private final Channel<TupstreamItem> upstreamProjection;
    
    
    private final BoundQuery<?,Tuple> downstreamQuery;
    private final BoundQuery<?,Tuple> upstreamQuery;
    
    
    @SuppressWarnings("unchecked")
    public RelationalMapChannel
      (Reflector<Tdownstream> reflector
      ,Channel<Tupstream> source
      ,Focus<?> focus
      )
      throws BindException
    { 
      super(reflector,source);
      
      this.sourceCollection
        =source.<CollectionDecorator>
          decorate(CollectionDecorator.class);
      
      this.downstreamSource
        =new ThreadLocalChannel<Tdownstream>(reflector);
      this.downstreamSourceCollection
        =downstreamSource.<CollectionDecorator>
          decorate(CollectionDecorator.class);
        
      

      


      downstreamInputItem
      =new ThreadLocalChannel<TdownstreamItem>
        (downstreamSourceCollection.getComponentReflector());
      
      upstreamProjection
        =resultFocus.<TupstreamItem>bind(RelationalMap.this.upstreamProjection);

      upstreamInputItem
        =new ThreadLocalChannel<TupstreamItem>
          (sourceCollection.getComponentReflector());
      
      try
      {
        downstreamQuery
          =queryable.query
            (RelationalMap.this.downstreamQuery
            ,focus.chain(upstreamInputItem)
            );
      
        upstreamQuery
          =queryable.query
            (RelationalMap.this.upstreamQuery
            ,focus.chain(downstreamInputItem)
            );
      }
      catch (DataException x)
      { throw new BindException("Error binding queries",x);
      }
      
    }

    @Override
    protected Tdownstream retrieve()
    {
      
      try
      {
        Tdownstream ret=downstreamSourceCollection.newCollection();
        for (TupstreamItem item : sourceCollection)
        {
          if (debugLevel.isFine())
          { log.fine("Mapping "+item);
          }
          upstreamInputItem.push(item);
          try
          {
            SerialCursor<Tuple> result=downstreamQuery.execute();
            while (result.next())
            { 
              if (debugLevel.isFine())
              { log.fine("Mapped to "+result.getTuple());
              }
              resultChannel.push(result.getTuple());
              try
              { ret=downstreamSourceCollection.add(ret,downstreamProjection.get());
              }
              finally
              {  resultChannel.pop();
              }
            }
          }
          finally
          { upstreamInputItem.pop();
          }
        }
        return ret;
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving data",x);
      }
      
    }

    @Override
    protected boolean store(
      Tdownstream val)
      throws AccessException
    {
      if (val==null)
      { return source.set(null);
      }
      
      downstreamSource.push(val);
      try
      {
        Tupstream ret=sourceCollection.newCollection();
        for (TdownstreamItem item : downstreamSourceCollection)
        {
          if (debugLevel.isFine())
          { log.fine("Reverse mapping "+item);
          }
          downstreamInputItem.push(item);
          try
          {
            SerialCursor<Tuple> result=upstreamQuery.execute();
            while (result.next())
            { 
              if (debugLevel.isFine())
              { log.fine("Mapped to "+result.getTuple());
              }
              resultChannel.push(result.getTuple());
              try
              { ret=sourceCollection.add(ret,upstreamProjection.get());
              }
              finally
              { resultChannel.pop();
              }
            }
          }
          finally
          { downstreamInputItem.pop();
          }
        }
        return source.set(ret);
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving data",x);
      }
      finally
      { downstreamSource.pop();
      }
      
    }
    
    @Override
    public boolean isWritable()
    { return source.isWritable();
    }
  }

}
