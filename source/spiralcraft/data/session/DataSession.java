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
package spiralcraft.data.session;


import spiralcraft.data.DataComposite;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Identifier;
import spiralcraft.data.Space;
import spiralcraft.data.Type;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.PojoIdentifier;
import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.util.DebugDataConsumer;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.refpool.URIPool;

import java.util.LinkedHashMap;
import java.net.URI;

/**
 * Represents the state of a data modification session that holds a
 *   transactional unit of work. 
 *
 * @author mike
 */
public class DataSession
{
  public static final URI FOCUS_URI
    =URIPool.create("class:/spiralcraft/data/session/DataSession");
  public static final ClassLog log=ClassLog.getInstance(DataSession.class);
  
  /**
   * Find the nearest DataSession in context
   * 
   * @param focus
   * @return
   */
  public static final Channel<DataSession> findChannel(Focus<?> focus)
  {
    Focus<DataSession> dsf=focus.findFocus(FOCUS_URI);
    if (dsf!=null)
    { return dsf.getSubject();
    }
    else
    { return null;
    }
  }
  
  private LinkedHashMap<Identifier,Buffer> buffers;  
  private DataComposite data;
  private Type<? extends DataComposite> type;
  private Space space;
  private DataSessionResourceManager resourceManager
    =new DataSessionResourceManager(this);
  boolean debug;
  
  
  public void setType(Type<? extends DataComposite> type)
  { this.type=type;
  }
  
  /**
   * The Space which the DataSession queries and updates
   * 
   * @param store
   */
  public void setSpace(Space space)
  { 
    this.space=space;
  }
  
  public Space getSpace()
  { return space;
  }
  
  public Type<? extends DataComposite> getType()
  { return type;
  }
  
  public DataComposite getData()
  { 
    if (data==null && type!=null)
    { data=new EditableArrayTuple(type.getScheme());
    }
    return data;
  }
  
  /**
   * Specify whether to log activity of this DataSession and related resource 
   *   managers.
   * 
   * @param debug
   */
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>Obtain a Buffer for the specified DataComposite. If an appropriate
   *   buffer is not found, create one and cache it in the session.
   * </p>
   * 
   * @param composite
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public synchronized Buffer buffer(DataComposite composite)
    throws DataException
  {
    if (debug)
    { log.fine("Buffering "+composite);
    }
    
    if (buffers==null)
    { buffers=new LinkedHashMap<Identifier,Buffer>();
    }
    
    Identifier id=composite.getId();
    if (id==null)
    { id=new PojoIdentifier(composite);
    }
    Buffer buffer=buffers.get(id);
    if (buffer==null || buffer.getOriginal()!=composite)
    { 
      if (debug && buffer!=null)
      { log.debug("Replacing stale buffer for "+composite);
      }
      
      if (composite.isTuple())
      { buffer=new BufferTuple(this,composite.asTuple());
      }
      else if (composite.isAggregate())
      { 
        if (composite.getType().getCoreType().isPrimitive())
        { 
          throw new DataException
            ("Cannot buffer primitive type "+composite.getType());
        }
        buffer=new BufferAggregate(this,composite.asAggregate());
      }
      else
      { 
        // Consider a Reference type
        throw new IllegalArgumentException("DataComposite not recognized");
      }
      if (debug)
      { log.fine("Using new buffer "+buffer);
      }
      buffers.put(composite.getId(), buffer);
    }
    else
    { 
      if (debug)
      { log.fine("Using existing buffer "+buffer);
      }
    }
    
    return buffer;
    
    
  }

  /**
   * <p>Return or create an Buffer associated with the specified Identifier.</p>
   * 
   * <p>If no buffer is associated with the Identifier, create a new one from the
   *   original DataComposite and associate it with the Identifier for
   *   future retrieval.
   * </p>
   * 
   * <p>Buffers will be cleared when a transaction involving this DataSession
   *   is committed.
   * </p>
   * 
   *   
   * @param <Tbuffer>
   * @param type
   * @param id
   * @param original
   * @return The existing buffer for the id, or a new buffer if one was
   *   created.
   * @throws DataException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public synchronized <Tbuffer extends Buffer> Tbuffer 
    bufferForId(Type<?> type,Identifier id,DataComposite original)
    throws DataException
  {
    if (buffers==null)
    { buffers=new LinkedHashMap<Identifier,Buffer>();
    }
    Buffer buffer=buffers.get(id);
    if (buffer!=null)
    { 
      if (debug)
      { log.fine("Using existing buffer "+buffer);
      }
      return (Tbuffer) buffer;
    }   
    
    if (original!=null)
    { 
      if (original.isTuple())
      { buffer=new BufferTuple(this,original.asTuple());
      }
      else if (original.isAggregate())
      { buffer=new BufferAggregate(this,original.asAggregate());
      }

    }
    else
    {
    
      if (type.isAggregate())
      { buffer=new BufferAggregate(this,type);
      }
      else
      { buffer=new BufferTuple(this,type);
      }
    
      if (id==null)
      { id=new PojoIdentifier(buffer);
      }
      buffer.setId(id);
    }
    
    buffers.put(buffer.getId(),buffer);
    if (debug)
    { log.fine("Created new buffer "+buffer);
    }
    return (Tbuffer) buffer;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public synchronized <Tbuffer extends Buffer> Tbuffer newBuffer(Type<?> type)
    throws DataException
  {
    if (buffers==null)
    { buffers=new LinkedHashMap<Identifier,Buffer>();
    }
    Buffer buffer=null;
    if (type.isAggregate())
    { buffer=new BufferAggregate(this,type);
    }
    else
    { buffer=new BufferTuple(this,type);
    }
    buffer.setId(new PojoIdentifier(buffer));
    
    buffers.put(buffer.getId(),buffer);
    if (debug)
    { log.fine("Created new buffer "+buffer);
    }
    return (Tbuffer) buffer;
  }
  
  /**
   * Remove the specified buffer from being cached, once the buffer has
   *   reverted, or it is known that it will not be used.
   *   
   * @param buffer
   * @param composite
   */
  synchronized void release(Buffer buffer,Identifier id)
  {
    if (buffers.get(id)==buffer)
    { 
      if (buffers.remove(id)!=null)
      {    
        if (debug)
        { log.fine("Released #"+id);
        }
      }
    }
  }
  
  /**
   * Save all edits in the DataSession to the store
   */
  public synchronized void save()
    throws DataException
  { 
    Transaction transaction
      =Transaction.startContextTransaction(Transaction.Nesting.ISOLATE);
    
//    DataSessionBranch branch=resourceManager.branch(transaction);
    
    try
    {
      DataConsumer<DeltaTuple> dataConsumer
        =new DebugDataConsumer<DeltaTuple>();
    
      for (Buffer buffer: buffers.values())
      {
        if (buffer.isTuple())
        { 
          if (buffer.asTuple().isDirty())
          { buffer.asTuple().save();
          }
        }
        dataConsumer.dataAvailable(buffer.asTuple());
        
      }
      
      dataConsumer.dataFinalize();
      transaction.commit();
    }
    catch (DataException x)
    { 
      transaction.rollback();
    }
    finally
    { 
      transaction.complete();
    }
    
    
   
  }
  
  void clearBuffers()
  { 
    if (buffers!=null)
    { buffers.clear();
    }
  }
  
  void writeTuple(BufferTuple t)
    throws DataException
  { 
    space.pushDataSession(this);
    try
    { writeTupleInContext(t);
    }
    finally
    { space.popDataSession();
    }    
  }
  
  
  private void writeTupleInContext(BufferTuple t)
    throws DataException
  { 
    if (!t.isDirty() || (t.isDelete() && t.getOriginal()==null))
    { return;
    }
    
    Transaction transaction
      =Transaction.getContextTransaction();
    
    if (transaction!=null)
    {
      if (debug)
      { log.fine("Saving "+t);
      }

      DataSessionBranch branch
        =resourceManager.branch(transaction);
      boolean first=branch.addBuffer(t);
      DeltaTuple delta=t;
      if (!first)
      { 
        log.fine("Multiple updates for buffer "+t+" rebasing");
        delta=t.rebase(t.deltaSnapshot.freeze());
      }
      
      DataConsumer<DeltaTuple> updater
        =branch.getUpdater(t.getType().getArchetype());
      if (updater!=null)
      { 
        boolean ok=false;
        try
        { 
          updater.dataAvailable(delta);
          if (first)
          { 
            // Snapshot after we set defaults
            t.deltaSnapshot=ArrayDeltaTuple.copy(t);
          }
          ok=true;
        }
        finally
        { 
          if (!ok)
          { transaction.rollbackOnComplete();
          }
        }
      }
      else
      { log.fine("No updater in Space for Type "+getType());
      }
    }
    else
    { 
      if (debug)
      { log.fine("Saving "+toString());
      }
      transaction=
        Transaction.startContextTransaction(Transaction.Nesting.ISOLATE);
      try
      {
        
        DataSessionBranch branch
          =resourceManager.branch(transaction);
        branch.addBuffer(t);
        
        boolean ok=false;
        try
        {
          DataConsumer<DeltaTuple> updater=branch.getUpdater(t.getType().getArchetype());
          if (updater!=null)
          { 
            updater.dataAvailable(t);
            transaction.commit();
          }
          else
          { log.fine("No updater in Space for Type "+getType());
          }
          if (debug)
          { log.fine("Finished commit of "+this);
          }
          ok=true;
        }
        finally
        {
          if (!ok)
          { transaction.rollback();
          }
        }
      }
      catch (DataException x)
      { 
        log.log(Level.FINE,"RE",x);
        throw x;
      }
      catch (RuntimeException x)
      { 
        log.log(Level.FINE,"RE",x);
        throw x;
      }
      finally
      {
        transaction.complete();
      }
    }
    
  }
  
  public void writeAggregate
    (BufferAggregate<? extends Buffer,? extends DataComposite> a)
    throws DataException
  { 
    space.pushDataSession(this);
    try
    { writeAggregateInContext(a);
    }
    finally
    { space.popDataSession();
    }
  }
  
  public void writeAggregateInContext
    (BufferAggregate<? extends Buffer,? extends DataComposite> a)
    throws DataException
  {
    if (debug)
    { log.fine("Saving..."+a);
    }
    
    Transaction transaction
      =Transaction.getContextTransaction();
  
    if (transaction!=null)
    {
      
      for (Buffer buffer: a)
      { 
        if (buffer.isTuple())
        { writeTupleInContext(buffer.asTuple());
        }
        else
        { writeAggregateInContext(buffer.asAggregate());
        }
      }
      
      
      DataSessionBranch branch
        =resourceManager.branch(transaction);
      branch.addBuffer(a);      
    }
    else
    { 
      transaction=
        Transaction.startContextTransaction(Transaction.Nesting.ISOLATE);
      try
      {
        for (Buffer buffer: a)
        { 
          if (buffer.isTuple())
          { writeTupleInContext(buffer.asTuple());
          }
          else
          { writeAggregateInContext(buffer.asAggregate());
          }
        }
      
        DataSessionBranch branch
          =resourceManager.branch(transaction);
        branch.addBuffer(a);

        transaction.commit();
      }
      finally
      {
        transaction.complete();
      }
    }
  }
}  
  
class DataSessionResourceManager
    extends ResourceManager<DataSessionBranch>
{

  private final DataSession session;
  
  public DataSessionResourceManager(DataSession session)
  { this.session=session;
  }
  
  @Override
  public DataSessionBranch createBranch(
    Transaction transaction)
    throws TransactionException
  { return new DataSessionBranch(session);
  }
    
}  
      
  
  
  
  
  
  

