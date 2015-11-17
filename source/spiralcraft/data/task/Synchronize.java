//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.data.task;


import spiralcraft.common.ContextualException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Aggregate.Index;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Scan;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.EditableKeyedListAggregate;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.kit.BindingSet;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.Level;
import spiralcraft.rules.Inspector;
import spiralcraft.rules.Rule;
import spiralcraft.rules.RuleException;
import spiralcraft.rules.RuleSet;
import spiralcraft.rules.Violation;
import spiralcraft.task.Chain;
import spiralcraft.util.KeyFunction;

/**
 * Synchronizes a set of data from an external source with a local set
 * 
 * @author mike
 *
 * @param <Tcontext>
 */
public class Synchronize
  extends Chain<Void,Void>
{
  private Binding<?> source;
  private IterationDecorator<?,Tuple> sourceIter;
  private Query query;
  private BoundQuery<?,Tuple> boundQuery;
  private Projection<Tuple> key;
  private Type<?> dataType;
  private Type<?> bufferType;
  private Channel<DataSession> dataSessionChannel;
  private ThreadLocalChannel<Aggregate<BufferTuple>> bufferChannel;
  private boolean autoSave;
  private BindingSet<Tuple> keyBindings;
  private RuleSet<Void,Tuple> insertRules;
  private Inspector<Void,Tuple> insertInspector;
  private RuleSet<Void,Tuple> sourceRules;
  private Inspector<Void,Tuple> sourceInspector;
  

  { importContext=true;
  }
  
  /**
   * An Expression binding which provides the external to synchronize with
   * 
   * @param source
   */
  public void setSource(Binding<?> source)
  { this.source=source;
  }
  
  /**
   * An array of binding expressions in the form " field := expr " that
   *   limit the set of data being synchronized 
   * 
   * @param keyBindings
   */
  public void setKeyBindings(Expression<?>[] keyBindings)
  { this.keyBindings=new BindingSet<Tuple>(keyBindings);
  }
  
  /**
   * Whether to save the buffers after sub-tasks have been completed
   */
  public void setAutoSave(boolean autoSave)
  { this.autoSave=autoSave;
  }

  /**
   * Validation rules for all incoming source entities
   * 
   * @param sourceRules
   */
  public void setSourceRules(Rule<Void,Tuple>[] sourceRules)
  { 
    this.sourceRules=new RuleSet<Void,Tuple>(null);
    this.sourceRules.addRules(sourceRules);
  }
  
  /**
   * Validation rules for source entities that will be created because they
   *   were not found in the local set.
   * 
   * @param sourceRules
   */
  public void setInsertRules(Rule<Void,Tuple>[] insertRules)
  {
    this.insertRules=new RuleSet<Void,Tuple>(null);
    this.insertRules.addRules(insertRules);
  }

  @SuppressWarnings({ "rawtypes", "unchecked"})
  @Override
  protected Focus<?> bindContext(Focus<?> focusChain)
    throws ContextualException
  {
    source.bind(focusChain);
    sourceIter=source.decorate(IterationDecorator.class);
    if (sourceIter==null)
    { throw new BindException("Source is not iterable");
    }
    
    if (sourceIter.getComponentReflector() instanceof DataReflector)
    { 
      dataType=((DataReflector) sourceIter.getComponentReflector()).getType();
      bufferType=Type.getBufferType(dataType);
    }
    
    if (key==null 
        && dataType!=null
       )
    { key=(Key<Tuple>) dataType.getPrimaryKey();
    }
    if (key==null)
    { 
      throw new BindException
        ("Could not derive a key from "
        +sourceIter.getComponentReflector().getTypeURI()
        );
    }
    

    if (keyBindings!=null)
    {
      keyBindings.setInputReflector
        ((DataReflector) DataReflector.getInstance(bufferType));
      keyBindings.bind(focusChain);
    }
    
    if (query==null && dataType!=null)
    { 
      if (keyBindings==null)
      { query=new Scan(dataType);
      }
      else
      { query=new EquiJoin(dataType,keyBindings.getBindingExpressions());
      }
    }
    
    if (query==null)
    { 
      throw new BindException
        ("No query specified or derivable");
    }
    
    boundQuery=(BoundQuery<?,Tuple>) query.bind(focusChain);
    
    dataSessionChannel=LangUtil.findChannel(DataSession.class,focusChain);
    if (dataSessionChannel==null)
    { throw new BindException("Must be within the scope of a data session");
    }
    
    if (sourceRules!=null)
    { 
      sourceInspector=sourceRules.bind
        (sourceIter.getComponentReflector()
        ,focusChain
        );
    }

    if (insertRules!=null)
    { 
      insertInspector=insertRules.bind
        (sourceIter.getComponentReflector()
        ,focusChain
        );
    }
    
    return focusChain;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> focusChain)
    throws ContextualException
  {
    
    bufferChannel=new ThreadLocalChannel<Aggregate<BufferTuple>>
      (DataReflector.<Aggregate<BufferTuple>>getInstance
        (Type.getAggregateType(bufferType))
      ,true
      ,focusChain.getSubject()
      );
    return focusChain.chain(bufferChannel);
  }
  
  public class SynchronizeTask
    extends ChainTask
  {

    @Override
    protected void work()
      throws InterruptedException
    { 
      DataSession dataSession
        =dataSessionChannel!=null
        ?dataSessionChannel.get()
        :null;
      
      try
      {
        EditableAggregate<BufferTuple> buffers
          =new EditableArrayListAggregate<BufferTuple>
            (bufferType);
            
        EditableKeyedListAggregate<Tuple> tempSet
          =new EditableKeyedListAggregate<Tuple>
            (Type.getAggregateType(boundQuery.getType()));
        
        
        SerialCursor<Tuple> cursor=boundQuery.execute();
        try
        { 
          while (cursor.next())
          {
            if (debug)
            { log.fine("Local set: "+cursor.getTuple());
            }
            tempSet.add(cursor.getTuple());
          }
        }
        finally
        { cursor.close();
        }
        
        Index<Tuple> index=tempSet.getIndex(key,true);
        KeyFunction<KeyTuple, Tuple> keyFunction=key.getKeyFunction();
        
        for (Tuple sourceTuple:sourceIter)
        {
          if (sourceInspector!=null)
          { 
            Violation<Tuple>[] violations=sourceInspector.inspect(sourceTuple);
            if (violations!=null && violations.length>0)
            { throw new RuleException(violations);
            }
          }
          KeyTuple sourceKey=keyFunction.key(sourceTuple);
          Tuple localTuple=index.getFirst(sourceKey);
          if (debug)
          { log.fine("Key is "+sourceKey);
          }
          
          if (localTuple==null)
          {
            if (debug)
            { log.fine("Adding: "+sourceTuple);
            }
            
            if (insertInspector!=null)
            { 
              Violation<Tuple>[] violations=insertInspector.inspect(sourceTuple);
              if (violations!=null && violations.length>0)
              { throw new RuleException(violations);
              }
            }
            
            // Create a new buffer
            if (dataSession!=null)
            { 
              BufferTuple buffer=dataSession.newBuffer(bufferType);
              buffer.updateFrom(sourceTuple);

              if (keyBindings!=null)
              { keyBindings.evaluate(buffer);
              }
              
              buffers.add(buffer);
            }
          }
          else
          {
            
            tempSet.remove(localTuple);
            if (debug)
            { log.fine("Synchronizing: "+localTuple+" <--- from <--- "+sourceTuple);
            }
            
            // Buffer the localTuple
            if (dataSession!=null)
            { 
              BufferTuple buffer=(BufferTuple) dataSession.buffer(localTuple);
              buffer.updateFrom(sourceTuple);
              if (keyBindings!=null)
              { keyBindings.evaluate(buffer);
              }
              buffers.add(buffer);
            }
            
          }
          
        }
        
        for (Tuple localTuple: tempSet)
        {
          if (debug)
          { log.fine("Missing "+localTuple);
          }
          BufferTuple buffer=(BufferTuple) dataSession.buffer(localTuple);
          buffer.delete();
          buffers.add(buffer);
          
        }
        
        bufferChannel.push(buffers);
        try
        { super.work();
        }
        finally
        { bufferChannel.pop();
        }
        
        if (autoSave)
        { 
          for (BufferTuple buffer: buffers)
          { 
            if (debug)
            { log.fine("Saving "+buffer);
            }
            buffer.save();
          }
        }
        
        
      }
      catch (DataException x)
      { 
        if (debug)
        { log.log(Level.WARNING,"Threw",x);
        }
        addException(x);
      }
      catch (RuleException x)
      {
        if (debug)
        { log.log(Level.WARNING,"Rule Violation "+x.getMessage(),x);
        }
        addException(x);
      }
    }
  }
    
  @Override
  protected SynchronizeTask task()
  { return new SynchronizeTask();
  }
}
