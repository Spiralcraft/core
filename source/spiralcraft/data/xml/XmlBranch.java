package spiralcraft.data.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Identifier;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.UniqueKeyViolationException;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.access.kit.AbstractStore;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.data.spi.EditableKeyedListAggregate;
import spiralcraft.data.spi.KeyedListAggregate;
import spiralcraft.data.spi.ListCursor;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.State;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.KeyFunction;

public class XmlBranch
  implements Branch
{
  private final ClassLog log=ClassLog.getInstance(XmlBranch.class);
  private Level logLevel=Level.INFO;
  
  private final XmlQueryable queryable;
  
  private ArrayList<String> resources=new ArrayList<String>();
  
  private ArrayList<Tuple> preparedAdds=new ArrayList<Tuple>();
  private ArrayList<JournalTuple> preparedUpdates=new ArrayList<JournalTuple>();
  
  private ArrayList<DeltaTuple> deltaList=new ArrayList<DeltaTuple>();
  
  private EditableKeyedListAggregate<DeltaTuple> deltaKeyedList;
  
  private HashMap<Identifier,DeltaTuple> deltaMap
    =new HashMap<Identifier,DeltaTuple>();
  
  private HashMap<Identifier,DeltaTuple> bufferMap
     =new HashMap<Identifier,DeltaTuple>();
  
  private ArrayList<ArrayJournalTuple> undoList
    =new ArrayList<ArrayJournalTuple>();
  
  private HashMap<Identifier,ArrayJournalTuple> baseRevs
    =new HashMap<Identifier,ArrayJournalTuple>();
  
  private LinkedHashSet<Identifier> wroteList
    =new LinkedHashSet<Identifier>();
  
  private EditableKeyedListAggregate<ArrayJournalTuple> txCopy;
  private KeyFunction<KeyTuple,Tuple> primaryKeyFn;
  private KeyedListAggregate.Index<ArrayJournalTuple> txPrimaryIndex;
      
  private HashSet<KeyTuple> insertedKeys=new HashSet<KeyTuple>();
  
  private State state=State.STARTED;
  
  @SuppressWarnings("unused")
  private AbstractStore.StoreBranch storeBranch;
  
  long txId;
  private Transaction transaction=Transaction.getContextTransaction();
  
  @SuppressWarnings("unchecked")
  XmlBranch(XmlQueryable queryable)
    throws TransactionException
  { 
    this.queryable=queryable;
    this.logLevel=queryable.getLogLevel();
    
    deltaKeyedList=new EditableKeyedListAggregate<DeltaTuple>
      (Type.getAggregateType(Type.getDeltaType(queryable.getResultType())));

    txCopy=new EditableKeyedListAggregate<ArrayJournalTuple>
      (Type.getAggregateType(queryable.getResultType()));
    Key<Tuple> primaryKey=(Key<Tuple>) queryable.getResultType().getPrimaryKey();
   
    try
    {
      if (primaryKey!=null)
      {
        primaryKeyFn=primaryKey.getKeyFunction();
        txPrimaryIndex=txCopy.getIndex
         ((Key<ArrayJournalTuple>) queryable.getResultType().getPrimaryKey(),true);
      }
    }
    catch (DataException x)
    { throw new TransactionException("Unable to get primary key index from "+queryable.getResultType());
    }
        
    try
    {
      for (Key<?> key: queryable.uniqueKeys)
      { deltaKeyedList.getIndex((Key<DeltaTuple>) key,true);
      }
    }
    catch (DataException x)
    { throw new TransactionException("Error creating delta indices",x);
    }

    queryable.setLock.lock();
    // Don't allow reloads or updates during a transaction
    queryable.freeze();
    

    
    // Make sure we have latest copy from disk
    boolean ok=false;
    try
    { 
      queryable.checkInit();
      queryable.watcher.check();
      ok=true;
    }
    catch (DataException x)
    {
      throw new TransactionException
        ("Error syncing with base resource "+queryable.resource.getURI(),x);
    }
    catch (IOException x)
    { 
      throw new TransactionException
        ("Error syncing with base resource "+queryable.resource.getURI(),x);
    }
    finally
    {
      if (!ok)
      { queryable.unfreeze();
      }
    }
  }

  SerialCursor<Tuple> getCursor()
    throws DataException
  { 
    if (deltaList.isEmpty())
    { return queryable.getPublicCursor();
    }
    
    // TODO: This only accounts for deletes
    KeyedListAggregate<Tuple> data=queryable.getAggregate();
    ArrayList<Tuple> result=new ArrayList<Tuple>(data.size());
    for (Tuple tuple: data)
    { 
      DeltaTuple delta=deltaMap.get(tuple.getId());
      if (delta==null || !delta.isDelete())
      { 
        tuple=getLatestTxVersion(tuple);
        result.add(tuple);
      }
      
    }
    
    if (primaryKeyFn!=null)
    {
      for (KeyTuple key: insertedKeys)
      {
        ArrayJournalTuple tuple=txPrimaryIndex.getFirst(key);
        if (tuple!=null)
        { result.add(tuple);
        }
      }
    }
    
    if (logLevel.isFine())
    { log.fine("Branch query for "+queryable.getResultType());
    }
    return new ListCursor<Tuple>(queryable.getResultType().getFieldSet(),result);

  }
  
  SerialCursor<Tuple> getCursor(Projection<Tuple> projection,KeyTuple key)
    throws DataException
  { 
    
    if (deltaList.isEmpty())
    { return queryable.getPublicCursor(projection,key);
    }
    
    // TODO: This only accounts for deletes and inserts
    KeyedListAggregate<Tuple> aggregate=queryable.getAggregate();
    Aggregate.Index<Tuple> index=aggregate.getIndex(projection,true);
    Aggregate<Tuple> data=index.get(key);
    if (data!=null)
    {
      ArrayList<Tuple> result=new ArrayList<Tuple>(data.size());
      for (Tuple tuple: data)
      { 
        DeltaTuple delta=deltaMap.get(tuple.getId());
      
        if (delta==null || !delta.isDelete())
        { 
          tuple=getLatestTxVersion(tuple);
          result.add(tuple);
        }
      }
      
      if (primaryKeyFn!=null)
      {
        if (logLevel.isFine())
        { log.fine("Checking for inserts in "+projection+" for "+key);
        }
        // Account for inserts in this transaction that match the index key
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aggregate.Index<ArrayJournalTuple> txIndex=txCopy.getIndex
          ((Projection) projection,true);
        Aggregate<ArrayJournalTuple> txResult=txIndex.get(key);
        if (txResult!=null)
        { 
          for (Tuple tuple: txResult)
          {
            if (insertedKeys.contains(primaryKeyFn.key(tuple)))
            { result.add(tuple);
            }
          }
        }
      }
      
      if (logLevel.isFine())
      { log.fine("Branch index query for "+projection.toString());
      }
      return new ListCursor<Tuple>(queryable.getResultType().getFieldSet(),result);
    }
    else
    { 
      if (primaryKeyFn!=null)
      { 
        // There's nothing in the store prior to this transaction. Check for
        //   inserts in this transaction.
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aggregate.Index<ArrayJournalTuple> txIndex=txCopy.getIndex
          ((Projection) projection,true);
        Aggregate<ArrayJournalTuple> txResult=txIndex.get(key);
        if (txResult!=null)
        { 
          ArrayList<Tuple> result=new ArrayList<Tuple>(txResult.size());
          for (Tuple tuple: txResult)
          { result.add(tuple);
          }
          return new ListCursor<Tuple>(queryable.getResultType().getFieldSet(),result);
        }
        
      }
      return queryable.emptyCursor;
    }
    
    
  }
  
  void addResource(String suffix)
  { resources.add(suffix);
  }

  void setStoreBranch(AbstractStore.StoreBranch storeBranch)
  {       
    this.storeBranch=storeBranch;
    this.txId=storeBranch.getTxId();     
  }

  private Aggregate<DeltaTuple> getUniqueDeltas(int indexNum,DeltaTuple tuple)
    throws DataException
  {       
    @SuppressWarnings("unchecked")
    Key<DeltaTuple> key=(Key<DeltaTuple>) queryable.uniqueKeys.get(indexNum);
    Aggregate.Index<DeltaTuple> index
      =deltaKeyedList.getIndex(key,true);
  
    Aggregate<DeltaTuple> results
      =index.get(key.getKeyFunction().key(tuple));

    return results;
  }
  
  private boolean hasSameKey(int indexNum,Tuple t1,Tuple t2)
  {
    @SuppressWarnings("unchecked")
    Key<Tuple> key=(Key<Tuple>) queryable.uniqueKeys.get(indexNum);
    KeyFunction<KeyTuple,Tuple> fn=key.getKeyFunction();
    return fn.key(t1).equals(fn.key(t2));
  }
  
  /**
   * Check data integrity constraints for an insert
   * 
   * @param tuple
   * @throws DataException
   */
  private void checkInsertIntegrity(DeltaTuple tuple)
    throws DataException
  {      
    // Check unique keys
    queryable.localData.push(tuple);
    try
    {
      int i=0;
      for (BoundQuery<?,Tuple> query: queryable.uniqueQueries)
      {
        SerialCursor<Tuple> cursor=query.execute();
        try
        {
          while (cursor.next())
          { 
            DeltaTuple txDelta=deltaMap.get(cursor.getTuple().getId());
            if (txDelta==null || !txDelta.isDelete())
            {
              // Make sure the conflicting row hasn't been deleted in this
              //   transaction.
              log.warning
                ("Unique conflict on add: "+tuple+":"+queryable.uniqueKeys.get(i)+" conflicts with "+txDelta);
              throw new UniqueKeyViolationException
                (tuple,queryable.uniqueKeys.get(i));
            }
          }
        }
        finally
        { cursor.close();
        } 
        
        
        // XXX This should be folded into the query's current transaction
        //   visibility
        Aggregate<DeltaTuple> uniqueDeltas
          =getUniqueDeltas(i,tuple);
        
        if (uniqueDeltas!=null)
        {
          for (DeltaTuple txDelta: uniqueDeltas)
          {
            if (!txDelta.isDelete()
                && !(txDelta.getId().equals(tuple.getId())
                     && txDelta.getOriginal()==null 
                     // XXX Allow multiple "inserts" of same id to coalesce 
                     //   for now
                    )
               )
            {
              log.warning
                ("Unique conflict on add: "+tuple+" conflicts with "+txDelta+": "+queryable.uniqueKeys.get(i));
              throw new UniqueKeyViolationException
                (tuple,queryable.uniqueKeys.get(i));
            }
          }
        }
        
        i++;
        
      }
    }
    finally
    { queryable.localData.pop();
    }

  }
  
  
  
  /**
   * Check data integrity constraints for an update
   * 
   * @param tuple
   * @throws DataException
   */
  private DeltaTuple checkUpdateIntegrity(DeltaTuple tuple)
    throws DataException
  {      
    queryable.localData.push(tuple);
    try
    {
      // Check unique keys
      int i=0;
      for (BoundQuery<?,Tuple> query: queryable.uniqueQueries)
      {
        SerialCursor<Tuple> cursor=query.execute();
        try
        {
          while (cursor.next())
          { 
            Identifier storeId=cursor.getTuple().getId();
            if (!storeId.equals(tuple.getId()) 
                && cursor.getTuple()!=tuple.getOriginal() 
                  // latter case is to avoid error when updating a tuple w/o
                  //   a primary key (id is based on POJO)
                )
            {
              DeltaTuple txDelta=deltaMap.get(storeId);
              if (txDelta==null
                  || (!txDelta.isDelete()
                      && hasSameKey(i,tuple,txDelta)
                     )
                 )
              {
                if (logLevel.isFine())
                { 
                  log.fine("\r\n  existing="+cursor.getTuple()+" id="+cursor.getTuple().getId()
                    +"\r\n  new="+tuple.getOriginal()+" id="+(tuple.getOriginal()!=null?tuple.getOriginal().getId():"?")
                    +"\r\n updated="+tuple+" id="+tuple.getId()
                    );
                }
                throw new UniqueKeyViolationException
                  (tuple,queryable.uniqueKeys.get(i));
              }
            }
          }
        }
        finally
        { cursor.close();
        }
        
        // XXX This should be folded into the query's current transaction
        //   visibility
        Aggregate<DeltaTuple> uniqueDeltas
          =getUniqueDeltas(i,tuple);
      
        if (uniqueDeltas!=null)
        {
          for (DeltaTuple txDelta: uniqueDeltas)
          {
            if (!txDelta.getId().equals(tuple.getId()) && !txDelta.isDelete())
            {
              log.fine("delta id = "+txDelta.getId()+"  id = "+tuple.getId());             
              log.warning
                  ("Unique conflict on update: "+tuple+" conflicts with "
                    +txDelta+": "+queryable.uniqueKeys.get(i));
              throw new UniqueKeyViolationException
                (tuple,queryable.uniqueKeys.get(i));
            }
          }
        }
              
        
        i++;
        
      }
      return tuple;
    }
    finally
    { queryable.localData.pop();
    }
  }
  
  private void addDelta(DeltaTuple tuple)
  { 
    DeltaTuple ot=deltaMap.get(tuple.getId());
    if (ot!=null)
    { deltaKeyedList.remove(ot);
    }

    deltaList.add(tuple);
    deltaMap.put(tuple.getId(),tuple);
    deltaKeyedList.add(tuple);
    if (!baseRevs.containsKey(tuple.getId()))
    { baseRevs.put(tuple.getId(),(ArrayJournalTuple) tuple.getOriginal());
    }
  }
  
  private void removeDelta(DeltaTuple tuple)
  { 
    deltaKeyedList.remove(tuple);
    deltaMap.remove(tuple.getId());
    deltaList.remove(tuple);
    if (tuple.getOriginal()==baseRevs.get(tuple.getId()))
    { baseRevs.remove(tuple.getId());
    }
  }

  Tuple getLatestTxVersion(Tuple storeVersion)
  {          
    if (primaryKeyFn!=null)
    {
      KeyTuple primaryKey=primaryKeyFn.key(storeVersion);
      ArrayJournalTuple txVersion=txPrimaryIndex.getFirst(primaryKey);
      if (txVersion!=null)
      { return txVersion;
      }
      else
      { return storeVersion;
      }
    }
    // TODO: To be correct, sequentially compare to everything to txVersion
    return storeVersion;
  }
  
  
  ArrayJournalTuple findInTx(Tuple original)
  {
    ArrayJournalTuple ret=null;
    if (primaryKeyFn!=null)
    { ret=txPrimaryIndex.getFirst(primaryKeyFn.key(original));
    }
    else
    { 
      for (ArrayJournalTuple tuple:txCopy)
      { 
        if (tuple.equals(original))
        { 
          ret=tuple;
          break;
        }
      }
      
    }
    if (ret!=null)
    { log.fine("Already in transaction "+original);
    }
    return ret;
  }
  
  /**
   * Perform implementation specific insert logic
   * 
   * @param tuple
   */
  void insert(DeltaTuple delta)
    throws DataException
  { 

    delta=checkBuffer(delta);
    checkInsertIntegrity(delta);
    ArrayJournalTuple newVersion=(ArrayJournalTuple) delta.freeze();
    txCopy.add(newVersion);
    if (primaryKeyFn!=null)
    { insertedKeys.add(primaryKeyFn.key(newVersion));
    }
    addDelta(delta);
  }
  
  /**
   * Perform implementation specific update logic
   * 
   * @param tuple
   */
  void update(DeltaTuple delta)
    throws DataException
  {
    delta=checkBuffer(delta);
    checkUpdateIntegrity(delta);
    delta=lockOriginal(delta);
    ArrayJournalTuple original=findInTx(delta.getOriginal());
    if (original!=null)
    {
        
      txCopy.replace
        (original
        ,((ArrayJournalTuple) delta.getOriginal()).getTxVersion()
        );
    }
    else
    { txCopy.add((ArrayJournalTuple) delta.getOriginal());
    }
    addDelta(delta);
  }
  
  
  /**
   * Perform implementation specific delete logic
   * 
   * @param tuple
   */
  void delete(DeltaTuple delta)
    throws DataException
  {       
    delta=checkBuffer(delta);
    delta=lockOriginal(delta);
    ArrayJournalTuple original=findInTx(delta.getOriginal());
    if (original!=null)
    { 
      txCopy.remove(original);
      if (primaryKeyFn!=null)
      { insertedKeys.remove(primaryKeyFn.key(original));
      }
    }
    addDelta(delta);
    
  }

  DeltaTuple checkBuffer(DeltaTuple tuple)
    throws DataException
  {
    if (tuple.isMutable())
    { 
      DeltaTuple newTuple=ArrayDeltaTuple.copy(tuple);
      bufferMap.put(newTuple.getId(),tuple);
      tuple=newTuple;
    }
    return tuple;
    
  }
  
  void notifyBuffer(Identifier id,Tuple newOriginal)
    throws TransactionException
  { 
    DeltaTuple buffer=bufferMap.get(id);
    if (buffer!=null)
    {
      try
      { buffer.updateOriginal(newOriginal);
      }
      catch (DataException x)
      { 
        throw new TransactionException("Error notifying buffer "+buffer+" of "
          +" new original "+newOriginal,x);
      }
    }
  }
  
  
  /**
   * Lock the original while we update it
   * 
   * @param tuple
   * @throws DataException
   */
  DeltaTuple lockOriginal(DeltaTuple tuple)
    throws DataException
  {
    if (!(tuple.getOriginal() instanceof JournalTuple))
    { 
      // TODO: Rebase the original to be friendly
      throw new DataException("Not a JournalTuple "+tuple.getOriginal());
    }
    JournalTuple jt=(JournalTuple) tuple.getOriginal();
    
    
    DeltaTuple prepared=jt.prepareUpdate(tuple);  
    
    if (logLevel.isDebug() && prepared!=tuple)
    { log.fine("Rebase during prepare:\r\n  "+prepared+"  \r\n  was  "+tuple);
    }
    
    if (prepared.getOriginal()!=null)
    {
      // Original might be rebased
      preparedUpdates.add((JournalTuple) prepared.getOriginal());
    }
    else
    { throw new DataException("Original is null for "+tuple);
    }

//      if (jt!= prepared.getOriginal() )
//      { log.fine("Added updated original "+prepared.getOriginal());
//      }
    return prepared;
  }
  
  @Override
  public void commit()
    throws TransactionException
  {
    if (state!=State.PREPARED)
    { 
      throw new TransactionException
        ("Commit can only be called when in PREPARED state, not "+state.name());
    }
    
    synchronized (queryable)
    {        
      for (String suffix:resources)
      { 
        
        try
        { 
          queryable.commit(suffix,txId);
          if (logLevel.isFine())
          { log.fine("Committed "+queryable.resource.getURI()+": "+suffix+" in "+txId);
          }
        }
        catch (IOException x)
        { throw new TransactionException("Error committing '"+suffix+"'",x);
        }
      }
      
      for (JournalTuple jt: preparedUpdates)
      { jt.commit();
      }

      state=State.COMMITTED;
    }

  }

  
  @Override
  public void complete()
  {
    try
    {
      if (state!=State.COMMITTED && state!=State.ABORTED)
      { rollback();
      }
    }
    catch (TransactionException x)
    { log.log(Level.WARNING,"Error rolling back incomplete transaction",x);
    }
    finally
    { 
      queryable.resourceManager.completed(this.transaction);
      queryable.unfreeze();
      queryable.setLock.unlock();
    }
    state=State.COMPLETED;

  }

  @Override
  public State getState()
  { return state;
  }

  @Override
  public boolean is2PC()
  { return true;
  }

  private void wrote(DeltaTuple tuple)
  {
    if (logLevel.isFine())
    { log.fine(txId+" Wrote "+tuple.getId()+" "+tuple);
    }
    wroteList.add(tuple.getId());
  }
  
  @Override
  public void prepare()
    throws TransactionException
  { 
    if (logLevel.isFine())
    { log.fine("Preparing "+queryable.resource.getURI()+" tx="+txId);
    }

    ArrayList<DeltaTuple> rebaseList=new ArrayList<DeltaTuple>();
    ArrayList<DeltaTuple> replaceList=new ArrayList<DeltaTuple>();
      
    
    for (DeltaTuple dt: deltaList)
    {
        
      try
      {
          
        Tuple orig=dt.getOriginal();
        // Handle case where the Store was reloaded during an edit
        JournalTuple storeVersion
          =(JournalTuple) queryable.getStoreVersion
            (orig!=null
            ?orig
            :dt
            );
        JournalTuple baseRev=baseRevs.get(dt.getId());
        
        if (storeVersion!=orig)
        {
          if (logLevel.isFine())
          { log.fine(txId+" Checking merge for id "+dt.getId()+" "
              +dt+"  \r\nstoreVersion="+storeVersion
            );
          }
          
          if ( (storeVersion==null) 
                || (orig!=null && !(orig instanceof JournalTuple))
                || (!storeVersion.isPreviousVersion((JournalTuple) orig)
                    && storeVersion.getTransactionId() 
                        != Transaction.getContextTransaction().getId()
                        // storeVersion is from current transaction
                   )
               )
           
          { 
            if (logLevel.isInfo())
            { 
              log.info
                ("Merging external store changes for "+dt+" to rebase "
                +orig+" to "+storeVersion+" (baseRev="+baseRev+")"
                );
            }
          
            if (orig instanceof JournalTuple)
            { 
              if (preparedUpdates.remove(orig))
              { ((JournalTuple) orig).rollback();
              }
            }
            rebaseList.add(dt);
            dt=dt.rebase(storeVersion);
            replaceList.add(dt);
            
            if (storeVersion!=null)
            {  
              storeVersion.prepareUpdate(dt);
              if (dt.getOriginal()!=null)
              {
                // Original might be rebased
                preparedUpdates.add((JournalTuple) dt.getOriginal());
              }
              else
              { throw new DataException("Original is null: "+dt);
              }
            }
            
          }
          else if (orig==null)
          { 
            dt=dt.updateOriginal(storeVersion);
            
            // An additional insert for a storeVersion that was placed
            // during this transaction
          }
        }
      }
      catch (DataException x)
      { throw new TransactionException("Error merging "+dt,x);
      }
      
        
      if (dt.getOriginal()!=null)
      {
        // 
        ArrayJournalTuple ot=(ArrayJournalTuple) dt.getOriginal();
        ArrayJournalTuple nt=ot.getTxVersion();
        if (nt==null)
        { 
          if (logLevel.isFine())
          { log.fine("Null txversion in "+ot);
          }
        }
        if (!dt.isDelete())
        { 
          // log.fine("Replacing "+ot+" with "+nt);
          queryable.replace(ot,nt);
          undoList.add(ot);
          wrote(dt);
          notifyBuffer(dt.getId(),nt);
        }
        else
        { 
          queryable.remove(ot);
          undoList.add(ot);
          wrote(dt);
          notifyBuffer(dt.getId(),null);
        }
      }
      else
      { 
        try
        { 
          if (logLevel.isFine())
          { log.fine("Type="+dt.getType().getURI());
          }
          ArrayJournalTuple at=(ArrayJournalTuple) dt.freeze();
          queryable.add(at);
          preparedAdds.add(at);
          wrote(dt);
          notifyBuffer(dt.getId(),at);
          
        }
        catch (DataException x)
        { throw new TransactionException("Error adding "+dt,x);
        }            
      }
    }
      
    for (DeltaTuple dt: rebaseList)
    { removeDelta(dt);           
    }
    for (DeltaTuple dt: replaceList)
    { addDelta(dt);           
    }
      
    try
    { 
      queryable.flushResource();
    }
    catch (DataException x)
    { throw new TransactionException("Error flushing resource .tx"+txId,x);
    }
    catch (IOException x)
    { throw new TransactionException("Error flushing resource .tx"+txId,x);
    }

    if (logLevel.isFine())
    { log.fine("Prepared "+queryable.resource.getURI()+": "+txId);
    }
    
    
    this.state=State.PREPARED;
  }

  @Override
  public void rollback()
    throws TransactionException
  { 

    
    if (state!=State.COMMITTED && state!=State.ABORTED)
    { 
      Collections.reverse(deltaList);
      for (DeltaTuple dt: deltaList)
      {
        if (dt.getOriginal()!=null)
        {
          ArrayJournalTuple ot=(ArrayJournalTuple) dt.getOriginal();
          if (!dt.isDelete())
          { 
            notifyBuffer(dt.getId(),ot);
          }
          else
          { 
            notifyBuffer(dt.getId(),ot);
          }
        }
      }
      Collections.reverse(undoList);
      for (ArrayJournalTuple ot:undoList)
      { 
        ArrayJournalTuple nt=ot.getTxVersion();
        if (nt==null || nt.isDeletedVersion())
        { queryable.add(ot);
        }
        else
        { queryable.replace(nt,ot);
        }
      }
      
      Collections.reverse(preparedUpdates);
      for (JournalTuple jt: preparedUpdates)
      { jt.rollback();
      }
      
      Collections.reverse(preparedAdds);
      for (Tuple t: preparedAdds)
      { queryable.remove(t);
      }
      
      for (String suffix:resources)
      { 
        try
        { queryable.rollback(suffix);
        }
        catch (IOException x)
        { log.log(Level.WARNING,"IOException rolling back '"+suffix+"'",x);
        }
      }
    }
    else
    { log.warning("Rollback on txid "+txId+" called in inapplicable state "+state);
    }

    this.state=Transaction.State.ABORTED;
  }
  
  @Override
  public String toString()
  { return super.toString()+": "
      +queryable.type.getURI()+" txid="+txId;
  }
}
