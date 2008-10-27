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
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.AggregateIndexTranslator;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.BufferAggregate;
import spiralcraft.data.session.BufferChannel;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.BufferType;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.Setter;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.log.ClassLogger;
import spiralcraft.util.thread.ContextFrame;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;





/**
 * Provides lifecycle management and WebUI control bindings for
 *   BufferTuples
 * 
 * @author mike
 *
 */
public class TupleEditor
  extends EditorBase<BufferTuple>
  implements FocusChainObject
{
  private static final ClassLogger log
    =ClassLogger.getInstance(TupleEditor.class);

  private Setter<?>[] fixedSetters;
  private Setter<?>[] initialSetters;
  private Setter<?>[] defaultSetters;
  private Setter<?>[] newSetters;
  private Setter<?>[] publishedSetters;
  private ContextFrame next;


  
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
  public Command<BufferTuple,Void> addAndClearCommand()
  { 
    return new CommandAdapter<BufferTuple,Void>()
    { 
      @Override
      public void run()
      { 
        addToParent();
        writeToModel(null);
      }
    };
      
  }
  
  public Command<BufferTuple,Void> deleteCommand()
  { 
    return new CommandAdapter<BufferTuple,Void>()
        {
          @Override
          public void run()
          { 
            try
            { 
              BufferTuple buffer=(BufferTuple) bufferChannel.get();
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
  
  public Command<BufferTuple,Void> deleteCommand
    (final Command<?,?> chainedCommand)
  { 
    return new CommandAdapter<BufferTuple,Void>()
    {
      @Override
      public void run()
      { 
        try
        { 
          BufferTuple buffer=(BufferTuple) bufferChannel.get();
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

  protected void ensureInitialized()
  {
    Buffer buffer=bufferChannel.get();
    
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
    
    Buffer buffer=bufferChannel.get();
    
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
      Buffer buffer=bufferChannel.get();

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

  /**
   * <p>Bind the Editor into the focus chain
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void bind
    (Focus<?> parentFocus)
      throws BindException
  { 
    if (debug)
    { log.fine("Editor.bind() "+parentFocus);
    }
    
    
    
    if (source==null)
    { 
      source=(Channel<Tuple>) parentFocus.getSubject();
      if (source==null)
      {
        log.fine
          ("No source specified, and parent Focus has no subject: "+parentFocus);
      }
    }
    
    
    if (source.getReflector() 
          instanceof DataReflector
        )
    { 
      DataReflector dataReflector=(DataReflector) source.getReflector();
      
      if ( dataReflector.getType() 
            instanceof BufferType
         ) 
      { 
        if (dataReflector.getType().isAggregate())
        {          
          bufferChannel
            =new TranslatorChannel
              (source
              ,new AggregateIndexTranslator(dataReflector)
              ,null // parentFocus.bind(indexExpression);
              );
          if (debug)
          { log.fine("Buffering indexed detail "+bufferChannel.getReflector());
          }
          Channel x=source;
          aggregateChannel=x;
          
        }
        else
        {
          if (debug)
          { log.fine("Using existing BufferChannel for "+source.getReflector());
          }
          Channel x=source;
          bufferChannel=x;
        }
      }
      else
      {
        if (debug)
        { log.fine("Creating BufferChannel for "+source.getReflector());
        }
        
        Channel x=source;
        bufferChannel=new BufferChannel
          ((Focus<DataComposite>) parentFocus
          ,(Channel<DataComposite>) x
          );
      }
      
      setupType();
    }
    
    if (bufferChannel==null)
    { 
      throw new BindException
        ("Not a DataReflector "
          +parentFocus.getSubject().getReflector()
        );
          
    }
    
    focus=parentFocus.chain(bufferChannel);
    setupSession(parentFocus);
    bindExports();
  }
  
  @SuppressWarnings("unchecked")
  protected void bindExports()
    throws BindException
  {
    DataReflector<Buffer> reflector
     =(DataReflector<Buffer>) getFocus().getSubject().getReflector();
  
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
    initialSetters=Assignment.bindArray(initialAssignments,focus);
    publishedSetters=Assignment.bindArray(publishedAssignments,focus);
    
  }
  
  @Override
  public <T> T runInContext(
    Delegate<T> delegate)
    throws DelegateException
  {
    // XXX Default implementation is inconvenient
    
    if (next!=null)
    { return next.runInContext(delegate);
    }
    else
    { return delegate.run();
    }
  }


  @Override
  public void setNext(
    ContextFrame next)
  { this.next=next;
  }  

}



