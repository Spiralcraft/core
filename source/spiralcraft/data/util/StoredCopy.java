//
// Copyright (c) 2013 Michael Toth
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


import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.editor.TupleEditor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Associates an external copy of an entity with a stored copy.
 * </p>
 * 
 * <p>When connected to an input that provides the external copy, this
 *   channel will output the stored copy with the same primary key value.
 * </p>
 * 
 * <p>When assigned a value, this channel will update the stored copy by
 *   copying the non-null fields of the assigned value.
 * </p>

 * @author mike
 *
 * @param <Tchannel>
 * @param <Tsource>
 */
public class StoredCopy
  implements ChannelFactory<Tuple,Tuple>,Contextual
{
  private static final ClassLog log
    =ClassLog.getInstance(RelationalMap.class);
  
  private Type<?> entityType;
  private Queryable<Tuple> queryable;
  private Query query;

  private ThreadLocalChannel<Tuple> inputChannel;
  private ThreadLocalChannel<Tuple> storeChannel;
  private TupleEditor editor;
  private Binding<?> preSave;
  
  private Level debugLevel=Level.INFO;
  
  public StoredCopy()
  {
  }
  
  public StoredCopy(DataReflector<Tuple> reflector)
  { this.entityType=reflector.getType();
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
  
  public void setPreSave(Expression<Void> preSave)
  { this.preSave=new Binding<Void>(preSave);
  }
  
  public StoredCopy
    (Type<Tuple> entityType)
  { 
    this.entityType=entityType;
  }

  public void setType(Type<?> entityType)
  { this.entityType=entityType;
  }
  

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    queryable=LangUtil.assertInstance(Space.class,focusChain);
    try
    {
      if (entityType.getPrimaryKey()==null)
      { 
        throw new DataException
          ("Type must have a primary key: "+entityType.getURI());
      }
      query=entityType.getPrimaryKey().getQuery();

  
      inputChannel=new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(entityType));
  
      storeChannel=new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(entityType));

      editor=new TupleEditor();
      editor.setAutoCreate(true);
      if (preSave!=null)
      { editor.setPreSave(preSave);
      }
      editor.bind(focusChain.chain(storeChannel));
      
    }
    catch (DataException x)
    { throw new BindException("Error resolving query",x);
    }
    return focusChain;
  }
  
  
  @SuppressWarnings("unchecked")
  @Override
  public Channel<Tuple> bindChannel(
    Channel<Tuple> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    try
    {
      return new StoredCopyChannel
          (source
          ,focus
          );
    }
    catch (ContextualException x)
    { throw new BindException("Error creating StoredCopyChannel",x);
    }
  }
  
  class StoredCopyChannel
    extends SourcedChannel<Tuple,Tuple>
  {



    private final BoundQuery<?,Tuple> boundQuery;


    @SuppressWarnings("unchecked")
    public StoredCopyChannel
      (Channel<Tuple> source
      ,Focus<?> focus
      )
      throws ContextualException
    { 
      super(inputChannel.getReflector(),source);


      boundQuery
        =queryable.query
        (StoredCopy.this.query
        ,focus.chain(inputChannel)
        );

    }

    
    private Tuple query(Tuple extern)
      throws DataException
    {         
      Tuple storeCopy=null;
      inputChannel.push(extern);
      try
      {
        SerialCursor<Tuple> result=boundQuery.execute();
        while (result.next())
        { 
          if (debugLevel.isFine())
          { log.fine("Mapped to "+result.getTuple());
          }
          if (storeCopy!=null)
          { 
            throw new AccessException
              ("Value '"+extern+"' maps to multiple results");
          }
          storeCopy=result.getTuple().snapshot();
        }
      }
      finally
      { inputChannel.pop();
      }
      return storeCopy;

    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Tuple retrieve()
    {

      try
      {
        
        Tuple input = source.get();
        if (input!=null)
        { return query(input);
        }
        else
        { return null;
        }
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving data",x);
      }

    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean store(Tuple val)
    {
      if (val==null)
      { return source.set(null);
      }

      Tuple stored=null;
      try
      { stored=query(val);
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving data",x);
      }
      
      
      storeChannel.push(stored);
      editor.push();
      try
      {
        editor.initBuffer();
        editor.getBuffer().updateFrom(val);
        editor.save(false);
        return true;
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving data",x);
      }
      finally
      {
        editor.pop();
        storeChannel.pop();
      }

    }

    @Override
    public boolean isWritable()
    { return source.isWritable();
    }
  }


}
