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
  private Expression<TupstreamItem> upstreamProjectionX;
  private Queryable<Tuple> queryable;
  
  private Focus<Tuple> resultFocus;
  private ThreadLocalChannel<Tuple> resultChannel;
  private Channel<TdownstreamItem> downstreamProjection;  

  private Level debugLevel=Level.INFO;
  private boolean unique;
  
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

  public void setUnique(boolean unique)
  { this.unique=unique;
  }
  
  public void setType(Type<?> entityType)
  { this.entityType=entityType;
  }
  
  /**
   * <p>Specify a field name if the data coming from the source represents field 
   *   value(s). When get() is issued, the set of source tuple(s) will be derived
   *   executing a join query against the field values from the source.
   * </p>
   *   
   * <p>If not specified, the data from the source is assumed to be the source
   *   tuple(s) themselves
   * </p>
   * @param fieldName
   */
  public void setUpstreamFieldName(String fieldName)
  { this.upstreamFieldName=fieldName;
  }

  /**
   * <p>Specify a field name if the data that is output by get() represents
   *   field value(s) of the source tuple(s). When set() is called with the
   *   set of field value(s), the source tuple(s) will be found by executing
   *   a join query against those values.
   * </p>
   *   
   * <p>If not specified, the data from the source is assumed to be the source
   *   tuple<s> themselves
   * </p>
   * @param fieldName
   */
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
      if (upstreamFieldName!=null)
      {
        downstreamQuery
          =new EquiJoin
            (entityType
            ,Expression.create("."+upstreamFieldName)
            ,Expression.create("..")
            );
        //downstreamQuery.setDebug(true);
        downstreamQuery.resolve();

        upstreamProjectionX=Expression.create("."+upstreamFieldName);
        
      }
      else
      { upstreamProjectionX=Expression.create(".");
      }
      
      
        
      if (downstreamFieldName!=null)
      {
        
    
        upstreamQuery
          =new EquiJoin
          (entityType
          ,Expression.create("."+downstreamFieldName)
          ,Expression.create("..")
          );
        upstreamQuery.resolve();
      

        downstreamProjectionX=Expression.create("."+downstreamFieldName);
      }
      else
      { downstreamProjectionX=Expression.create(".");
      }
      
      
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
    
    if (!unique)
    {
        
      return new RelationalMapChannel
        ((Reflector<Tdownstream>) 
            ArrayReflector.getInstance(downstreamProjection.getReflector())
        ,source
        ,focus
        );
    }
    else
    {
      return new UniqueRelationalMapChannel
      (downstreamProjection.getReflector()
      ,source
      ,focus
      );
    }
  }
  
  class UniqueRelationalMapChannel
    extends SourcedChannel<Tupstream,Tdownstream>
  {

    private final ThreadLocalChannel<TdownstreamItem> downstreamSource;


    private final ThreadLocalChannel<Tupstream> upstreamInputItem;
    private final ThreadLocalChannel<TdownstreamItem> downstreamInputItem;



    private final Channel<TupstreamItem> upstreamProjection;


    private final BoundQuery<?,Tuple> downstreamQuery;
    private final BoundQuery<?,Tuple> upstreamQuery;


    @SuppressWarnings("unchecked")
    public UniqueRelationalMapChannel
    (Reflector<TdownstreamItem> reflector
      ,Channel<Tupstream> source
      ,Focus<?> focus
    )
    throws BindException
    { 
      super((Reflector<Tdownstream>) reflector,source);


      this.downstreamSource
        =new ThreadLocalChannel<TdownstreamItem>(reflector);

      downstreamInputItem
      =new ThreadLocalChannel<TdownstreamItem>
        (downstreamSource.getReflector());

      upstreamProjection
      =resultFocus.<TupstreamItem>bind(RelationalMap.this.upstreamProjectionX);

      upstreamInputItem
      =new ThreadLocalChannel<Tupstream>
      (source.getReflector());

      try
      {
        if (RelationalMap.this.downstreamQuery!=null)
        {
          downstreamQuery
            =queryable.query
            (RelationalMap.this.downstreamQuery
            ,focus.chain(upstreamInputItem)
            );
        }
        else
        { downstreamQuery=null;
        }
        
        
        if (RelationalMap.this.upstreamQuery!=null)
        {

          upstreamQuery
            =queryable.query
            (RelationalMap.this.upstreamQuery
            ,focus.chain(downstreamInputItem)
            );
        }
        else
        { upstreamQuery=null;
        }
      }
      catch (DataException x)
      { throw new BindException("Error binding queries",x);
      }

    }

    @SuppressWarnings("unchecked")
    @Override
    protected Tdownstream retrieve()
    {

      try
      {
        Tdownstream ret=null;
        Tupstream item = source.get();
        
        if (debugLevel.isFine())
        { log.fine("Mapping "+item);
        }
        upstreamInputItem.push(item);
        try
        {
          if (downstreamQuery!=null)
          {
            SerialCursor<Tuple> result=downstreamQuery.execute();
            while (result.next())
            { 
              if (debugLevel.isFine())
              { log.fine("Mapped to "+result.getTuple());
              }
              if (ret!=null)
              { 
                throw new AccessException
                  ("Value '"+item+"' maps to multiple results");
              }
              
              resultChannel.push(result.getTuple().snapshot());
              try
              { ret=(Tdownstream) downstreamProjection.get();
              }
              finally
              {  resultChannel.pop();
              }
            }
          }
          else
          { 
            resultChannel.push((Tuple) item);
            try
            { ret=(Tdownstream) downstreamProjection.get();
            }
            finally
            {  resultChannel.pop();
            }            
          }
        }
        finally
        { upstreamInputItem.pop();
        }
        
        return ret;
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving data",x);
      }

    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean store(
      Tdownstream val)
    throws AccessException
    {
      if (val==null)
      { return source.set(null);
      }

      downstreamSource.push((TdownstreamItem) val);
      try
      {
        Tupstream ret=null;
        TdownstreamItem item = (TdownstreamItem) val;
        {
          if (debugLevel.isFine())
          { log.fine("Reverse mapping "+item);
          }
          downstreamInputItem.push(item);
          try
          {
            if (upstreamQuery!=null)
            {
              SerialCursor<Tuple> result=upstreamQuery.execute();
              while (result.next())
              { 
                if (debugLevel.isFine())
                { log.fine("Mapped to "+result.getTuple());
                }
                if (ret!=null)
                { 
                  throw new AccessException
                    ("Value '"+item+"' maps to multiple results");
                }
                
                resultChannel.push(result.getTuple());
                try
                { ret=(Tupstream) upstreamProjection.get();
                }
                finally
                { resultChannel.pop();
                }
              }
            }
            else
            {
              resultChannel.push((Tuple) item);
              try
              { ret=(Tupstream) upstreamProjection.get();
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
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        =resultFocus.<TupstreamItem>bind(RelationalMap.this.upstreamProjectionX);

      upstreamInputItem
        =new ThreadLocalChannel<TupstreamItem>
          (sourceCollection.getComponentReflector());
      
      try
      {
        if (RelationalMap.this.downstreamQuery!=null)
        {

          downstreamQuery
            =queryable.query
              (RelationalMap.this.downstreamQuery
              ,focus.chain(upstreamInputItem)
              );
        }
        else
        { downstreamQuery=null;
        }
        
        if (RelationalMap.this.upstreamQuery!=null)
        {

          upstreamQuery
            =queryable.query
              (RelationalMap.this.upstreamQuery
              ,focus.chain(downstreamInputItem)
              );
        }
        else
        { upstreamQuery=null;
        }
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
            if (downstreamQuery!=null)
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
            else
            {
              resultChannel.push((Tuple) item);
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
            if (upstreamQuery!=null)
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
            else
            {
              resultChannel.push((Tuple) item);
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
