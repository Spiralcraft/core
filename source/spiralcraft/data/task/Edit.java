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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
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
  private Binding<?> onSave;
  private Binding<?> preSave;
  private boolean forceSave;
  
  { storeResults=true;
  }
  
  public Edit()
  { 
  }
  
  @SuppressWarnings("unchecked")
  public Edit(Type<Titem> type)
  { 
    this.type=type;
    if (!type.isAggregate())
    { editor=(EditorBase<Tbuffer>) new TupleEditor();
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

  public void setOnSave(Binding<?> onSave)
  { this.onSave=onSave;
  }

  public void setPreSave(Binding<?> preSave)
  { this.preSave=preSave;
  }
  
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
    if (onSave!=null)
    { editor.setOnSave(onSave);
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
        
        
        editor.push();
        try
        {
          editor.initBuffer();
          super.work();
          if (autoSave)
          { 
            try
            { editor.save(forceSave);
            }
            catch (DataException x)
            { addException(x);
            }
          }
          addResult(editor.getBuffer());
        }
        catch (DataException x)
        { addException(x);
        }
        finally
        { editor.pop();
        }
      }
    };
  }
  
}
