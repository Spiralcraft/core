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
import spiralcraft.data.Tuple;
//import spiralcraft.data.Type;
import spiralcraft.data.editor.TupleEditor;
import spiralcraft.data.session.BufferChannel;
import spiralcraft.data.session.BufferTuple;
//import spiralcraft.data.lang.DataReflector;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;


/**
 * <p>Edits a Tuple
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
public class Edit<Titem extends Tuple>
  extends Scenario
{

  private Expression<Titem> targetX;
  private Channel<BufferTuple> resultChannel;
//  private Type<Titem> type;
  private ThreadLocalChannel<BufferTuple> localChannel;
  private TupleEditor editor
    =new TupleEditor();
  private boolean autoSave;
  private boolean autoCreate;
  
  
  public void setAutoSave(boolean autoSave)
  { this.autoSave=autoSave;
  }
  
  public void setAutoCreate(boolean autoCreate)
  { this.autoCreate=autoCreate;
  }

  /**
   * 
   * @return The TupleEditor 
   */
  public TupleEditor getEditor()
  { return editor;
  }
  
  /**
   * The target of this Collector, which must be of type Aggregate<?>
   * 
   * @param resultAssignment
   */
  public void setX
    (Expression<Titem> targetX)
  { this.targetX=targetX;
  }



//  @SuppressWarnings("unchecked") // Type query
  @Override
  public void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    if (targetX!=null)
    { 
      resultChannel
        =new BufferChannel<BufferTuple>(focusChain,focusChain.bind(targetX));
    }
//    if (type==null && resultChannel!=null)
//    { type=((DataReflector) resultChannel.getReflector()).getType();
//    }
    
    localChannel
      =new ThreadLocalChannel<BufferTuple>
        (resultChannel.getReflector()
        ,true
        );
    editor.setSource(localChannel);
    editor.bind(focusChain);
    super.bindChildren(focusChain.chain(localChannel));
  }

  
  @Override
  protected Task task()
  {
    return new ChainTask()
    {
        
      @Override
      public void work()
        throws InterruptedException
      {
        
        BufferTuple result=resultChannel.get();
        
        localChannel.push(result);
        try
        {
          if (autoCreate && result==null)
          { 
            Command<?,?> newCommand=editor.newCommand();
            newCommand.execute();
            if (newCommand.getException()!=null)
            { addException(newCommand.getException());
            }
          }
          super.work();
          if (autoSave)
          { 
            Command<?,?> saveCommand=editor.saveCommand();
            saveCommand.execute();
            if (saveCommand.getException()!=null)
            { addException(saveCommand.getException());
            }
          }
        }
        finally
        { localChannel.pop();
        }
      }
    };
  }
  
}
