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
import spiralcraft.data.Type;
//import spiralcraft.data.Type;
import spiralcraft.data.editor.TupleEditor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.BufferChannel;
import spiralcraft.data.session.BufferTuple;
//import spiralcraft.data.lang.DataReflector;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.Chain;
import spiralcraft.task.Task;


/**
 * <p>Edits a Tuple
 * </p>
 * 
 * <p>Publishes an TupleEditor of the specified type in the FocusChain, where
 *   it can be retrieved and populated and/or accessed by subtasks.
 * </p>
 * 
 * @author mike
 *
 */
public class Edit<Titem extends Tuple>
  extends Chain<Titem,BufferTuple>
{

  private Expression<Titem> targetX;
  private Channel<BufferTuple> resultChannel;
  private Type<Titem> type;
  private ThreadLocalChannel<BufferTuple> localChannel;
  private TupleEditor editor
    =new TupleEditor();
  private boolean autoSave;
  private boolean autoCreate;
  
  { storeResults=true;
  }
  
  public Edit()
  { 
  }
  
  public Edit(Type<Titem> type)
  { 
    this.type=type;
    autoCreate=true;
  }
  
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
   * The target of this Editor which must be a Tuple
   * 
   * @param resultAssignment
   */
  public void setX
    (Expression<Titem> targetX)
  { this.targetX=targetX;
  }


  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bindImports(Focus<?> focusChain)
    throws BindException
  {
    if (targetX!=null)
    { 
      resultChannel
        =new BufferChannel<BufferTuple>(focusChain,focusChain.bind(targetX));

    }
    else if (type!=null)
    { 
      resultChannel
        =new BufferChannel<BufferTuple>
          (focusChain
          ,DataReflector.<Titem>getInstance(type).getNilChannel()
          );
    }
    else
    {
      resultChannel
        =new BufferChannel<BufferTuple>
          (focusChain,(Channel<Tuple>) focusChain.getSubject());
      
      
    }
    resultReflector
      =resultChannel.getReflector();
    return focusChain;
    
  }
  

//  @SuppressWarnings("unchecked") // Type query
  @Override
  public Focus<?> bindExports(
    Focus<?> focusChain)
    throws BindException
  {
    
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
    return focusChain.chain(localChannel);
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
            Command<?,?,?> newCommand=editor.newCommand();
            newCommand.execute();
            if (newCommand.getException()!=null)
            { addException(newCommand.getException());
            }
          }
          super.work();
          if (autoSave)
          { 
            Command<?,?,?> saveCommand=editor.saveCommand();
            saveCommand.execute();
            if (saveCommand.getException()!=null)
            { addException(saveCommand.getException());
            }
          }
          addResult(localChannel.get());
        }
        finally
        { localChannel.pop();
        }
      }
    };
  }
  
}
