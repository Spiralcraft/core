//
// Copyright (c) 2009,2009 Michael Toth
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


import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Type;
import spiralcraft.data.lang.CursorChannel;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.ListCursor;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.Chain;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;


/**
 * <p>Manages the lifecycle of an Aggregate that collects data output by 
 *   subtasks.
 * </p>
 * 
 * <p>Publishes an Aggregate of the specified type in the FocusChain, where
 *   it can be retrieved and populated and/or accessed by subtasks.
 * </p>
 * 
 * <p>Creates an instance of the Aggregate per-execution, unless append=true
 *   which specifies that the Aggregate should be re-used.
 * </p>
 *   
 * <p>If append=true, the target will be read to obtain an existing Aggregate.
 *   If no existing Aggregate is found, one will be created.
 * </p>
 * 
 * @author mike
 *
 */
public class Collect<Titem>
  extends Chain<Void,Aggregate<Titem>>
{

  private Expression<Aggregate<Titem>> targetX;
  private Channel<Aggregate<Titem>> resultChannel;
  private Type<EditableAggregate<Titem>> type;
  private boolean append;
  private ThreadLocalChannel<EditableAggregate<Titem>> aggregateChannel;

  private Scenario<?,?> fillScenario;
  private ClosureFocus<?> closureFocus;
  private Expression<Titem> cursorX;
  @SuppressWarnings("unchecked")
  private CursorChannel cursorChannel;
  private Channel<Integer> expectedSizeChannel;
  private Expression<Integer> expectedSizeX;
  
  { storeResults=true;
  }
  
  public Collect()
  { 
  }
  
  public Collect(Scenario<?,?>[] chain)
  { setChain(chain);
  }
  
  /**
   * The target of this Collector, which must be of type Aggregate<?>
   *   
   * 
   * @param resultAssignment
   */
  public void setAggregateX
    (Expression<Aggregate<Titem>> targetX)
  { this.targetX=targetX;
  }

  public void setCursorX
    (Expression<Titem> cursorX)
  { this.cursorX=cursorX;
  }

  /**
   * Create a Lazy List and run the specified scenario to fetch
   *   the next set of elements. 
   * 
   * @param fillScenario
   */
  public void setFillScenario(Scenario<?,?> fillScenario)
  { this.fillScenario=fillScenario;
  }
  
  /**
   * An Expression which provides the expected size of the Lazy List. The
   *   Expression will be evaluated after nested scenarios are completed.
   * 
   * @param expectedSizeX
   */
  public void setExpectedSizeX(Expression<Integer> expectedSizeX)
  { this.expectedSizeX=expectedSizeX;
  }
  
  /**
   * Indicate that existing data from the target channel should be preserved.
   *
   * If set to false (the default), the aggregate will be empty at the
   *   start of the Task.
   * 
   * @param append
   */
  public void setAppend(boolean append)
  { this.append=append;
  }

  
  /**
   * Command that is referenceable from subtasks to add an item.
   * 
   * @param item
   * @return
   */
  public Command<Void,Collect<Titem>,Void> commandAdd(final Titem item)
  { 
    if (debug)
    { log.debug("Returning command "+item);
    }
    
    return new CommandAdapter<Void,Collect<Titem>,Void>()
    {
      @Override
      protected void run()
      { 
        if (debug)
        { log.debug("Adding "+item);
        }
        aggregateChannel.get().add(item);
      } 
    };
  }

  @SuppressWarnings("unchecked") // Type query
  @Override
  public void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    if (targetX!=null)
    { 
      resultChannel=focusChain.bind(targetX);
    }
    if (type==null && resultChannel!=null)
    { type=((DataReflector) resultChannel.getReflector()).getType();
    }
    
    if (cursorX!=null)
    { cursorChannel=(CursorChannel) focusChain.bind(cursorX);
    }
    aggregateChannel
      =new ThreadLocalChannel<EditableAggregate<Titem>>
        (DataReflector.<EditableAggregate<Titem>>getInstance(type)
        ,true
        );
    focusChain=focusChain.chain(aggregateChannel);
    if (fillScenario!=null)
    { 
      closureFocus=new ClosureFocus(focusChain);
      focusChain=closureFocus;
      fillScenario.bind(focusChain);
    }
    if (expectedSizeX!=null)
    { expectedSizeChannel=focusChain.bind(expectedSizeX);
    }
    super.bindChildren(focusChain.chain(aggregateChannel));
  }

  
  @Override
  protected Task task()
  {
    return new ChainTask()
    {
        
      @SuppressWarnings("unchecked")
      @Override
      public void work()
        throws InterruptedException
      {
        
        EditableAggregate<Titem> result=null;
        
        if (append)
        { result=(EditableAggregate<Titem>) resultChannel.get();
        }
        
        if (result==null)
        { 
          EditableArrayListAggregate<Titem> newResult
            =new EditableArrayListAggregate<Titem>(type);
          
          if (debug)
          { newResult.setDebug(debug);
          }
          
          if (fillScenario!=null)
          { 
            aggregateChannel.push(newResult);
            try
            { newResult.setFillCommand(new FillCommandChannel(newResult));
            }
            finally
            { aggregateChannel.pop();
            }
          }
          
          result=newResult;
          
          resultChannel.set(result);

        }
        
        aggregateChannel.push(result);
        try
        {
          super.work();
          if (cursorChannel!=null)
          { cursorChannel.setCursor(new ListCursor(result));
          }
          
          if (expectedSizeChannel!=null)
          { 
            if (result instanceof EditableArrayListAggregate)
            { 
              Integer expectedSize=expectedSizeChannel.get();
              if (expectedSize!=null)
              { 
                ((EditableArrayListAggregate) result)
                  .setExpectedSize(expectedSize);
              }
            }
          }
          addResult(aggregateChannel.get());
        }
        catch (DataException x)
        { addException(x);
        }
        finally
        { aggregateChannel.pop();
        }
      }
    };
  }
  
  /**
   * Encloses the context at the time of creation. Allocated to a single
   *   Aggregate object as the callback.
   *   
   * @author mike
   *
   */
  public class FillCommandChannel
    extends AbstractChannel<Command<?,?,?>>
  {

    private final ClosureFocus<?>.Closure closure;
    private final EditableArrayListAggregate<Titem> aggregate;
    
    public FillCommandChannel(EditableArrayListAggregate<Titem> aggregate)
    { 

      super(BeanReflector.<Command<?,?,?>>getInstance(Command.class));      
      closure=closureFocus.enclose();
      this.aggregate=aggregate;
    
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Command<?,?,?> retrieve()
    { 
      return new CommandAdapter()
      {

        @Override
        protected void run()
        { 
          closure.push();
          aggregateChannel.push(aggregate);
          try
          { 
            
            Command<?,?,?> command=fillScenario.command();
            command.execute();
            if (command.getException()!=null)
            { setException(command.getException());
            }
          }
          finally
          { 
            aggregateChannel.pop();
            closure.pop();
          }
          
          
        }
      };

    }

    @Override
    protected boolean store(
      Command<?,?,?> val)
      throws AccessException
    {
      // TODO Auto-generated method stub
      return false;
    }
  }
  
}
