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


import spiralcraft.common.Constant;
import spiralcraft.common.ContextualException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
//import spiralcraft.data.Type;
import spiralcraft.data.editor.AggregateEditor;
import spiralcraft.data.editor.EditorBase;
import spiralcraft.data.editor.TupleEditor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.BufferChannel;
//import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.session.DataSession;
import spiralcraft.data.session.DataSessionFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.task.CommandTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;


/**
 * <p>Deletes a Tuple
 * </p>
 * 
 * @author mike
 *
 */
public class Delete<Titem extends DataComposite,Tbuffer extends Buffer>
  extends Scenario<Titem,Tbuffer>
{

  private Expression<Titem> targetX;
  private Channel<Tbuffer> resultChannel;
  private Type<Titem> type;
  private EditorBase<Tbuffer> editor;
  private Binding<?> onCreate;
  private Binding<?> onInit;
  private Binding<?> afterSave;
  private Binding<?> preSave;
  private DataSessionFocus localDataSessionFocus;
  private ThreadLocalChannel<DataSession> localDataSessionChannel;
  
  { storeResults=true;
  }
  
  public Delete()
  { 
  }
  
  public void setOnInit(Binding<?> onInit)
  { this.onInit=onInit;
  }

  /**
   * An expression to evaluate after the buffer is deleted, but before the
   *   transaction is committed.
   * 
   * @param afterSave
   */
  public void setAfterDelete(Binding<?> afterSave)
  { this.afterSave=afterSave;
  }

  /**
   * An expression to evaluated before the buffer is deleted (e.g. to make additional
   *   changes based on data in the buffer.)
   * 
   * @param preSave
   */
  public void setPreDelete(Binding<?> preSave)
  { this.preSave=preSave;
  }
  
  
  /**
   * 
   * @return The TupleEditor 
   */
  @Constant
  public EditorBase<Tbuffer> getEditor()
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
    Channel<DataSession> dataSessionChannel
      =DataSession.findChannel(focusChain);
    if (dataSessionChannel==null)
    { 
      localDataSessionChannel=new ThreadLocalChannel<>
        (BeanReflector.<DataSession>getInstance(DataSession.class));
      localDataSessionFocus=new DataSessionFocus(focusChain,localDataSessionChannel,null);
      focusChain.addFacet(localDataSessionFocus);
    }
      
    
    if (targetX!=null)
    { 
      resultChannel
        =new BufferChannel<Tbuffer>(focusChain,focusChain.bind(targetX));

    }
    else if (type!=null)
    { 
      resultChannel
        =new BufferChannel<Tbuffer>
          (focusChain
          ,DataReflector.<Titem>getInstance(type).createNilChannel()
          );
    }
    else
    {
      resultChannel
        =new BufferChannel<Tbuffer>
          (focusChain,(Channel<Titem>) focusChain.getSubject());
      
      
    }
    resultReflector
      =resultChannel.getReflector();
    return focusChain;
    
  }
  

  @SuppressWarnings("unchecked") // Type query
  @Override
  public Focus<?> bindExports(
    Focus<?> focusChain)
    throws ContextualException
  {
    
    if (type==null && resultChannel!=null)
    { type=((DataReflector<Titem>) resultChannel.getReflector()).getType();
    }
    
    if (editor==null)
    {
      if (type.isAggregate())
      { editor=(EditorBase<Tbuffer>) new AggregateEditor();
      }
      else
      { editor=(EditorBase<Tbuffer>) new TupleEditor();
      }
    }

    editor.setSource(resultChannel);
    editor.setDebug(this.debug);
    if (onCreate!=null)
    { editor.setOnCreate(onCreate);
    }
    if (onInit!=null)
    { editor.setOnInit(onInit);
    }
    if (afterSave!=null)
    { editor.setAfterSave(afterSave);
    }
    if (preSave!=null)
    { editor.setPreSave(preSave);
    }
    focusChain=editor.bind(focusChain);

    focusChain.addFacet
      (focusChain.chain(LangUtil.constantChannel(editor)));
    return focusChain;
  }

  
  @Override
  protected Task task()
  {
    return new CommandTask()
    {

      @Override
      public void work()
        throws InterruptedException
      {
        if (localDataSessionChannel!=null)
        { localDataSessionChannel.push(localDataSessionFocus.newDataSession());
        }
        
        editor.push();
        try
        {
          editor.initBuffer();

          if (editor.getBuffer()!=null)
          { editor.getBuffer().delete();
            try
            { editor.save(true);
            }
            catch (DataException x)
            { addException(new ContextualException("Error deleting buffer",getDeclarationInfo(),x));
            }
          }
          else
          { log.warning("No buffer to delete: "+getDeclarationInfo());
          }
          
          addResult(editor.getBuffer());
        }
        catch (DataException x)
        { addException(x);
        }
        finally
        { 
          editor.pop();
          if (localDataSessionChannel!=null)
          { localDataSessionChannel.pop();
          }
        }
      }
    };
  }
  
}
