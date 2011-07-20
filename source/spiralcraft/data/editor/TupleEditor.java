//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.editor;


import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.BufferAggregate;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Setter;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.log.ClassLog;

/**
 * Provides lifecycle management and WebUI control bindings for
 *   BufferTuples
 * 
 * @author mike
 *
 */
public class TupleEditor
  extends EditorBase<BufferTuple>
  implements Contextual
{
  private static final ClassLog log
    =ClassLog.getInstance(TupleEditor.class);

  private Setter<?>[] fixedSetters;
  private Setter<?>[] initialSetters;
  private Setter<?>[] defaultSetters;
  private Setter<?>[] newSetters;
  private Setter<?>[] publishedSetters;
  private BindingChannel<?>[] newBindings;

  private BindingChannel<?>[] preSaveBindings;

  
  private Channel<BufferAggregate<Buffer,?>> aggregateChannel;
  
  private boolean phantom;
  
  /**
   * The contained buffer will not be saved by this editor. Usually used in
   *   conjunction with setDetail(true) to create an Editor that adds a new
   *   value to a parent BufferAggregate. 
   * 
   * @param val
   */
  public void setPhantom(boolean val)
  { phantom=val;
  }
  

  protected void publish()
  {  
    if (publishedSetters!=null)
    {
      if (debug)
      { log.fine(toString()+": applying published assignments");
      }
      for (Setter<?> setter: publishedSetters)
      { setter.set();
      }
    }
 
  }
    
  /**
   * <p>Adds the referenced Buffer to the parent BufferAggregate and
   *   clears the Editor to accommodate a new Tuple
   * </p>
   * 
   * @return A new Command
   */
  public Command<BufferTuple,Void,Void> addAndClearCommand()
  { 
    return new CommandAdapter<BufferTuple,Void,Void>()
    { 
      @Override
      public void run()
      { 
        addToParent();
        writeToModel(null);
      }
    };
      
  }
  
  public Command<BufferTuple,Void,Void> deleteCommand()
  { 
    return new CommandAdapter<BufferTuple,Void,Void>()
        {
          @Override
          public void run()
          { 
            try
            { 
              BufferTuple buffer=localChannel.get();
              if (buffer!=null)
              { buffer.delete();
              }
            }
            catch (Exception x)
            { setException(x);
            }
          }
        };
  }
  
  public Command<BufferTuple,Void,Void> deleteCommand
    (final Command<?,?,?> chainedCommand)
  { 
    return new CommandAdapter<BufferTuple,Void,Void>()
    {
      @Override
      public void run()
      { 
        try
        { 
          BufferTuple buffer=localChannel.get();
          if (buffer!=null)
          { buffer.delete();
          }
          if (chainedCommand!=null)
          { chainedCommand.execute();
          }
        }
        catch (Exception x)
        { setException(x);
        }
      }
    };
  }

  @Override
  protected void ensureInitialized()
  {
    Buffer buffer=localChannel.get();
    
    if (buffer!=null)
    {
      if (!buffer.isDirty())
      {
        if (newSetters!=null && buffer.getOriginal()==null)
        { 
          if (debug)
          { log.fine(toString()+": applying new values");
          }
          
          for (Setter<?> setter : newSetters)
          { setter.set();
          }
        }
        
        if (newBindings!=null && buffer.getOriginal()==null)
        {
          if (debug)
          { log.fine(toString()+": applying new values");
          }
          
          BindingChannel.apply(newBindings);
        }
        
        
        if (initialSetters!=null)
        {
          if (debug)
          { log.fine(toString()+": applying initial values");
          }

          for (Setter<?> setter : initialSetters)
          { setter.set();
          }
        }
      }
    }

  }
  
      
  public void setNewBindings(BindingChannel<?>[] bindings)
  { this.newBindings=bindings;
  }
  
  
  public void setPreSaveBindings(BindingChannel<?>[] bindings)
  { this.preSaveBindings=bindings;
  }
  
  
  @Override
  public void save(boolean force)
    throws DataException
  {
    if (phantom)
    {
      if (debug)
      { log.fine("Editor with phantom=true skipping save. "+toString());
      }
    }
    
    Buffer buffer=localChannel.get();
    
    if (buffer!=null && preSaveBindings!=null)
    { BindingChannel.apply(preSaveBindings);
    }
    
    applyKeyValues();
    
    if (buffer!=null && (buffer.isDirty() || force))
    {
      if (defaultSetters!=null)
      { 
        if (debug)
        { log.fine(toString()+": applying default values");
        }
        for (Setter<?> setter: defaultSetters)
        { 
          if (setter.getTarget().get()==null)
          { setter.set();
          }
        }
      
      }

      if (fixedSetters!=null)
      {
        if (debug)
        { log.fine(toString()+": applying fixed values");
        }
        for (Setter<?> setter: fixedSetters)
        { setter.set();
        }
      }
      
      
      buffer.save();
      
      if (publishedAssignments!=null)
      {
        if (debug)
        { log.fine(toString()+": applying published assignments post-save");
        }
        for (Setter<?> setter: publishedSetters)
        { setter.set();
        }
      }      
    }
    else
    { 
      if (buffer==null)
      {
        log.warning
          ("No buffer exists to save- no data read- try Editor.autoCreate");
      }
      else
      {
        if (debug)
        { log.fine("Not dirty "+buffer.toString());
        }
      }
      
    }
    
  }  

  /**
   * <p>Adds a buffer to a parent AggregateBuffer
   * </p>
   * 
   */
  protected void addToParent()
  {
    if (aggregateChannel!=null)
    {
      BufferAggregate<Buffer,?> aggregate=aggregateChannel.get();
      Buffer buffer=localChannel.get();

      if (aggregate!=null && buffer!=null)
      { 
        if (debug)
        { log.fine("Adding buffer to parent "+aggregate+": buffer="+buffer);
        }
        aggregate.add(buffer);
      }
      else
      {
        if (aggregate==null)
        {
          if (debug)
          { log.fine("Not adding buffer to null parent: buffer="+buffer);
          }
        }
        else
        {
          if (debug)
          { log.fine("Not adding null buffer to parent "+aggregate);
          }
        }
      }
      
    }
  }
    
  /**
   * Add a new empty buffer to the parent 
   */
  protected void addNewBufferToParentContainer()
    throws DataException
  {
    if (aggregateChannel!=null)
    {
      aggregateChannel.get()
        .add(getDataSession().newBuffer(getType().getContentType()));
    }
  }  

  
  @SuppressWarnings("unchecked")
  @Override
  protected Focus<?> bindExports(Focus<?> focus)
    throws ContextualException
  {
    DataReflector<Buffer> reflector
     =(DataReflector<Buffer>) focus.getSubject().getReflector();
  
    Type<?> type=reflector.getType();

    if (!type.isAggregate() && type.getScheme()!=null)
    { 
      // TODO: Used to traverse fields here, now that's done by the updater
      //   restructure this
      
    }
    else
    { 
      if (type.isAggregate())
      { throw new BindException
          ("Cannot bind a TupleEditor to an aggregate type "+type);
      }
    }
    
    fixedSetters=Assignment.bindArray(fixedAssignments,focus);
    defaultSetters=Assignment.bindArray(defaultAssignments,focus);
    newSetters=Assignment.bindArray(newAssignments,focus);
    BindingChannel.bindTarget(newBindings,focus);
    BindingChannel.bindTarget(preSaveBindings,focus);

    initialSetters=Assignment.bindArray(initialAssignments,focus);
    publishedSetters=Assignment.bindArray(publishedAssignments,focus);
    bindKeys(focus);
    return super.bindExports(focus);
  }

}



