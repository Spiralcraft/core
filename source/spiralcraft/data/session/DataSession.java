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
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Identifier;
import spiralcraft.data.Space;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.PojoIdentifier;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.State;
import spiralcraft.data.util.DebugDataConsumer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    =URI.create("class:/spiralcraft/data/session/DataSession");
  
  private HashMap<Identifier,Buffer> buffers;  
  private DataComposite data;
  private Type<DataComposite> type;
  private Space space;
  private ResourceManager<DataSessionBranch> resourceManager
    =new DataSessionResourceManager();
  private DataSessionFocus focus;
  
  public void setType(Type<DataComposite> type)
  { this.type=type;
  }

  public void setFocus(DataSessionFocus focus)
  { this.focus=focus;
  }
  
  /**
   * The Space which the DataSession queries and updates
   * 
   * @param store
   */
  public void setSpace(Space space)
  { this.space=space;
  }
  
  public Space getSpace()
  { return space;
  }
  
  public Type<DataComposite> getType()
  { return type;
  }
  
  public DataComposite getData()
  { 
    if (data==null)
    { data=new EditableArrayTuple(type.getScheme());
    }
    return data;
  }
  
  public ResourceManager<DataSessionBranch> getResourceManager()
  { return resourceManager;
  }
  
  /**
   * <p>Obtain a Buffer for the specified DataComposite. If an appropriate
   *   buffer is not found, create one and cache it in the session.
   * </p>
   * 
   * @param composite
   * @return
   */
  @SuppressWarnings("unchecked")
  public synchronized Buffer buffer(DataComposite composite)
    throws DataException
  {
    if (buffers==null)
    { buffers=new HashMap<Identifier,Buffer>();
    }
    
    Identifier id=composite.getId();
    if (id==null)
    { id=new PojoIdentifier(composite);
    }
    Buffer buffer=buffers.get(id);
    if (buffer==null)
    { 
      if (composite.isTuple())
      { buffer=new BufferTuple(this,composite.asTuple());
      }
      else if (composite.isAggregate())
      { buffer=new BufferAggregate(this,composite.asAggregate());
      }
      else
      { 
        // Consider a Reference type
        throw new IllegalArgumentException("DataComposite not recognized");
      }
      buffers.put(composite.getId(), buffer);
    }
    return buffer;
    
    
  }
  
  @SuppressWarnings("unchecked")
  public synchronized Buffer newBuffer(Type<?> type)
    throws DataException
  {
    Buffer buffer=null;
    if (type.isAggregate())
    { buffer=new BufferAggregate(this,type);
    }
    else
    { buffer=new BufferTuple(this,type);
    }
    buffer.setId(new PojoIdentifier(buffer));
    
    buffers.put(buffer.getId(),buffer);
    return buffer;
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
    { buffers.remove(id);
    }
  }
  
  /**
   * Saves changes to the store
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
  
  class DataSessionResourceManager
    extends ResourceManager<DataSessionBranch>
  {

    @Override
    public DataSessionBranch createBranch(
      Transaction transaction)
      throws TransactionException
    { return new DataSessionBranch();
    }
    
  }

  class DataSessionBranch
    implements Branch
  {

    private ArrayList<BufferTuple> buffers
      =new ArrayList<BufferTuple>();
    
    private HashMap<Type<?>,DataConsumer<DeltaTuple>> updaterMap
      =new HashMap<Type<?>,DataConsumer<DeltaTuple>>();
    
    private State state=State.STARTED;

    public void addBuffer(BufferTuple buffer)
    { buffers.add(buffer);
    }
    
    public  DataConsumer<DeltaTuple> getUpdater(Type<?> type)
      throws DataException
    {
      
      DataConsumer<DeltaTuple> updater
        =updaterMap.get(type);
      if (updater==null)
      { 
        updater=space.getUpdater(type,focus);
        if (updater!=null)
        { 
          updaterMap.put(type,updater);
          updater.dataInitialize(type.getScheme());
        }
      }
      return updater;
    }
    
    @Override
    public void prepare()
      throws TransactionException
    {
      Iterator<BufferTuple> it=buffers.iterator();
      while (it.hasNext())
      { 
        BufferTuple tuple=it.next();
        try
        { tuple.prepare();
        }
        catch (DataException x)
        { throw new TransactionException("Error preparing "+tuple,x);
        }
      }
      
      for (DataConsumer<DeltaTuple> updater : updaterMap.values())
      { 
        try
        { updater.dataFinalize();
        }
        catch (DataException x)
        { throw new TransactionException("Error finalizing updator "+updater,x);
        }
        
      }      
      state=State.PREPARED;
      
      
    }

    @Override
    public void rollback()
      throws TransactionException
    {
      // TODO Auto-generated method stub
      Iterator<BufferTuple> it=buffers.iterator();
      while (it.hasNext())
      { 
        it.next().rollback();
        it.remove();
      }
      state=State.ABORTED;
      
    }
    
    @Override
    public void commit()
      throws TransactionException
    {
      Iterator<BufferTuple> it=buffers.iterator();
      while (it.hasNext())
      { 
        BufferTuple buffer=it.next();
        try
        { buffer.commit();
        }
        catch (DataException x)
        { throw new TransactionException("Error committing "+buffer,x);
        }
        it.remove();
      }
      

      
      state=State.COMMITTED;
    }

    @Override
    public void complete()
    {
      if (!buffers.isEmpty())
      { 
        Iterator<BufferTuple> it=buffers.iterator();
        while (it.hasNext())
        { 
          it.next().rollback();
          it.remove();
        }
      }
      updaterMap.clear();
      // TODO Auto-generated method stub
    }

    @Override
    public State getState()
    { return state;
    }

    @Override
    public boolean is2PC()
    { return true;
    }


  }
  
  
  
}  
  
  
  
  
  
  

