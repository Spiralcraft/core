package spiralcraft.data.access.kit;

import java.net.URI;

import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Sequence;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.util.LangUtil;

public abstract class AbstractStoreSequence
  implements Sequence
{

  private int increment;
  private volatile long next;
  private volatile long stop;  
  protected final Focus<URI> uriFocus;
  protected final URI uri;
  protected BoundQuery<?,Tuple> boundQuery;  
  protected final AbstractStore store;
  private volatile boolean allocated=false;
  private final Object synchronizer;

  public AbstractStoreSequence (AbstractStore store,URI uri,Object synchronizer)
  { 
    this.store=store;
    this.uri=uri;
    uriFocus=new SimpleFocus<URI>(LangUtil.constantChannel(uri));
    this.synchronizer=synchronizer;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    try
    { 
      boundQuery=bindQuery(store.sequenceQuery);
      if (boundQuery==null)
      { 
        throw new LifecycleException
          ("Got null for sequence query "+store.sequenceQuery);
      }
    }
    catch (DataException x)
    { 
      throw new LifecycleException
        ("Error binding sequence query for "+uri,x);
    }    
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    try
    {
      if (allocated)
      { deallocate();
      }
    }
    catch (DataException x)
    { 
      throw new LifecycleException
        ("Error deallocating sequence "+uri,x);
    }
  }
    
  protected abstract BoundQuery<?,Tuple> bindQuery(Query sequenceQuery)
    throws DataException;
  
  protected abstract void updateInTx(DeltaTuple dt)
    throws TransactionException,DataException;
  
  protected abstract void insertInTx(DeltaTuple dt)
    throws TransactionException,DataException;

  public void deallocate()
    throws DataException
  {
    synchronized(synchronizer)
    {
      Transaction.startContextTransaction(Nesting.ISOLATE);
      try
      {
        SerialCursor<Tuple> result=boundQuery.execute();
        EditableTuple row=null;
        Tuple oldRow=null;
        
        try
        {
          if (!result.next())
          {
          }
          else
          {
            oldRow=result.getTuple().snapshot();
            row=new EditableArrayTuple(oldRow);
            row.set("nextValue",next);
            if (result.next())
            {
              throw new DataException
                ("Cardinality violation in Sequence store- non unique URI "+uri); 
            }
            
          }
        }
        finally
        { result.close();
        }
        

        if (oldRow!=null && row!=null)
        {
          DeltaTuple dt=new ArrayDeltaTuple(oldRow,row);        
          updateInTx(dt);
          Transaction.getContextTransaction().commit();        
        }
        stop=next;
        allocated=false;
      }
      finally
      { Transaction.getContextTransaction().complete();
      }
      
    }

  }
    
  public void allocate()
    throws DataException
  {
    synchronized(synchronizer)
    {

      Transaction.startContextTransaction(Nesting.ISOLATE);
      try
      {
        SerialCursor<Tuple> result=boundQuery.execute();

        EditableTuple row=null;
        Tuple oldRow=null;
        
        long newNext;
        long newStop;
        int newIncrement;
        
        try
        {
          if (!result.next())
          {
            row=new EditableArrayTuple(store.sequenceType);
            
            row.set("uri",uri);
            row.set("nextValue",200L);
            row.set("increment",100);

            newNext=100;
            newStop=200;
            newIncrement=100;

          }
          else
          {
            oldRow=result.getTuple().snapshot();
            row=new EditableArrayTuple(oldRow);
            
            newNext=(Long) row.get("nextValue");
            newIncrement=(Integer) row.get("increment");
          
            newStop=newNext+increment;
            row.set("nextValue",newNext+newIncrement);            
          
            if (result.next())
            {
              throw new DataException
                ("Cardinality violation in Sequence store- non unique URI "+uri); 
            }
          }
        
        }
        finally
        { result.close();
        }
      

        DeltaTuple dt=new ArrayDeltaTuple(oldRow,row);
        if (oldRow!=null)
        { updateInTx(dt);   
        }
        else
        { insertInTx(dt);   
        }
        Transaction.getContextTransaction().commit();
        
        next=newNext;
        stop=newStop;
        increment=newIncrement;
        allocated=true;
      }
      finally
      { Transaction.getContextTransaction().complete();
      }
      
    }
  }
    
    
    
  @Override
  public synchronized Long next()
    throws DataException
  {
    synchronized (synchronizer)
    {
      if (next>=stop || !allocated)
      { allocate();
      }
      return next++;
    }
  }  
}
