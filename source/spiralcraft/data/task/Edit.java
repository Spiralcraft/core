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
public class Edit<Titem extends DataComposite,Tbuffer extends Buffer>
  extends Chain<Titem,Tbuffer>
{

  private Expression<Titem> targetX;
  private Binding<Type<?>> typeX;
  private Channel<Tbuffer> resultChannel;
  private Type<Titem> type;
  private EditorBase<Tbuffer> editor;
  private boolean autoSave;
  private boolean autoCreate;
  private boolean autoKey;
  private Binding<?> onCreate;
  private Binding<?> onInit;
  private Binding<?> afterSave;
  private Binding<?> preSave;
  private boolean forceSave;
  private DataSessionFocus localDataSessionFocus;
  private ThreadLocalChannel<DataSession> localDataSessionChannel;
  private boolean chainAfterSave;
  
  { storeResults=true;
  }
  
  public Edit()
  { 
  }
  
  public Edit(DataReflector<Titem> reflector)
  { this(reflector.getType());
  }

  @SuppressWarnings("unchecked")
  public Edit(Type<Titem> type)
  { 
    this.type=type;
    if (!type.isAggregate())
    { editor=(EditorBase<Tbuffer>) new TupleEditor();
    }
    else
    { editor=(EditorBase<Tbuffer>) new AggregateEditor();
    }
    editor.setAutoCreate(true);
  }
  
  public void setType(Type<Titem> type)
  { this.type=type;
  }
  
  public void setTypeX(Binding<Type<?>> typeX)
  { this.typeX=typeX;
  }
  
  public void setAutoSave(boolean autoSave)
  { this.autoSave=autoSave;
  }
  
  public void setAutoCreate(boolean autoCreate)
  { this.autoCreate=autoCreate;
  }
  
  public void setAutoKey(boolean autoKey)
  { this.autoKey=autoKey;
  }

  public void setOnCreate(Binding<?> onCreate)
  { this.onCreate=onCreate;
  }

  public void setOnInit(Binding<?> onInit)
  { this.onInit=onInit;
  }

  /**
   * Run any chained tasks after auto-save so any data changed or added during the
   *   buffer flush can be picked up.
   *   
   * @param chainAfterSave
   */
  public void setChainAfterSave(boolean chainAfterSave)
  { this.chainAfterSave=chainAfterSave;
  }
  
  /**
   * Indicate that the specified action should always be performed and the buffer
   *   should always be saved. Equivalent to setting preSave and forceSave.
   * 
   * @param action
   */
  public void setAction(Binding<?> action)
  { 
    this.setPreSave(action);
    this.setForceSave(true);
  }
  
  /**
   * Use afterSave property- this is deprecated
   * 
   * @param onSave
   */
  public void setOnSave(Binding<?> onSave)
  { this.afterSave=onSave;
  }

  /**
   * An expression to evaluate after the buffer is saved, but before the
   *   transaction is committed.
   * 
   * @param afterSave
   */
  public void setAfterSave(Binding<?> afterSave)
  { this.afterSave=afterSave;
  }

  /**
   * An expression to evaluated before the buffer is saved (e.g. to make additional
   *   changes to data in the buffer.)
   * 
   * @param preSave
   */
  public void setPreSave(Binding<?> preSave)
  { this.preSave=preSave;
  }
  
  /** 
   * Push the buffer to the store even if it isn't dirty.
   * 
   * @param forceSave
   */
  public void setForceSave(boolean forceSave)
  { 
    this.forceSave=forceSave;
    if (this.forceSave)
    { this.autoSave=true;
    }
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
      
    if (typeX!=null)
    { 
      typeX.bind(focusChain);
      if (!typeX.isConstant())
      { throw new BindException("typeX must be constant");
      }
      type=(Type<Titem>) typeX.get();
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
      autoCreate=true;
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
      editor.setAutoCreate(autoCreate);
    }

    editor.setAutoKey(autoKey);
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
    return new ChainTask()
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
          if (!chainAfterSave)
          { super.work();
          }
          if (autoSave)
          { 
            if (debug)
            { log.fine("Auto-saving "+editor.getBuffer());
            }
            try
            { editor.save(forceSave);
            }
            catch (DataException x)
            { addException(new ContextualException("Error saving data",getDeclarationInfo(),x));
            }
          }
          if (chainAfterSave)
          { super.work();
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
