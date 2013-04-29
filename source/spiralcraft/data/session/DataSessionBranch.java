package spiralcraft.data.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Type;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.State;

public class DataSessionBranch
  implements Branch
{

  private ArrayList<Buffer> branchBuffers
    =new ArrayList<Buffer>();
  
  private HashMap<Type<?>,DataConsumer<DeltaTuple>> updaterMap
    =new HashMap<Type<?>,DataConsumer<DeltaTuple>>();
  
  private State state=State.STARTED;
  
  private final DataSession dataSession;

  public DataSessionBranch(DataSession session)
  { this.dataSession=session;
  }
  
  public void addBuffer(Buffer buffer)
  { branchBuffers.add(buffer);
  }
  
  public  DataConsumer<DeltaTuple> getUpdater(Type<?> type)
    throws DataException
  {
    
    DataConsumer<DeltaTuple> updater
      =updaterMap.get(type);
    if (updater==null)
    { 
      updater=dataSession.getSpace().getUpdater(type);
      if (updater!=null)
      { 
        updaterMap.put(type,updater);
        if (dataSession.debug)
        { updater.setDebug(true);
        }
        updater.dataInitialize(type.getFieldSet());
      }
    }
    return updater;
  }
  
  @Override
  public void prepare()
    throws TransactionException
  {
    Iterator<Buffer> it=branchBuffers.iterator();
    while (it.hasNext())
    { 
      Buffer buffer=it.next();
      try
      { buffer.prepare();
      }
      catch (DataException x)
      { throw new TransactionException("Error preparing "+buffer,x);
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
    Iterator<Buffer> it=branchBuffers.iterator();
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
    Iterator<Buffer> it=branchBuffers.iterator();
    while (it.hasNext())
    { 
      Buffer buffer=it.next();
      try
      { buffer.commit();
      }
      catch (DataException x)
      { throw new TransactionException("Error committing "+buffer,x);
      }
      it.remove();
    }
    
    dataSession.clearBuffers();

    state=State.COMMITTED;
  }

  @Override
  public void complete()
  {
    if (!branchBuffers.isEmpty())
    { 
      Iterator<Buffer> it=branchBuffers.iterator();
      while (it.hasNext())
      { 
        it.next().rollback();
        it.remove();
      }
    }
    updaterMap.clear();
    


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
