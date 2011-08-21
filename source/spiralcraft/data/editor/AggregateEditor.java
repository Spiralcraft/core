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


import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandFactory;
import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.BufferAggregate;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Setter;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;

/**
 * Provides lifecycle management and WebUI control bindings for
 *   BufferTuples
 * 
 * @author mike
 *
 */
public class AggregateEditor
  extends EditorBase<BufferAggregate<BufferTuple,Tuple>>
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

  
  private ThreadLocalChannel<BufferTuple> childChannel;
  private boolean contentRequired;
  
  public final CommandFactory<AggregateEditor,Void,BufferTuple> addNew
    =new AbstractCommandFactory<AggregateEditor,Void,BufferTuple>()
  {

    @Override
    public Reflector<BufferTuple> getResultReflector()
    { return childChannel.getReflector();
    }
    
    @Override
    public Command<AggregateEditor, Void, BufferTuple> command()
    { 
      return new CommandAdapter<AggregateEditor,Void,BufferTuple>()
      { 
        
        @Override
        public void run()
        { 
          try
          { 
            BufferTuple buffer
              =(BufferTuple) 
                getDataSession().newBuffer(getType().getContentType());
            
            BufferAggregate<BufferTuple,Tuple> aggregate=localChannel.get();
            if (aggregate==null)
            { newBuffer();
            }
            localChannel.get().add(buffer);
            setResult(buffer);
          }
          catch (DataException x)
          { setException(x);
          }
        }
      };      
    }
  };

  

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
  protected void save(boolean force)
    throws DataException
  {
    BufferAggregate<BufferTuple,Tuple> aggregate=localChannel.get();
    
    if (aggregate!=null)
    {
      if (preSaveBindings!=null || autoKey)
      {
        for (int i=0;i<aggregate.size();i++)
        { 
          BufferTuple buffer=aggregate.get(i);
          childChannel.push(buffer);
          try 
          { 
            if (preSaveBindings!=null)
            {
              if (debug)
              { log.fine(toString()+": applying preSaveAssignments to "+buffer);
              }
              BindingChannel.apply(preSaveBindings);
            }
            
            applyKeyValues();
          }
          finally
          { childChannel.pop();
          }
        }
      }

      
      
      // beforeCheckDirty(aggregate);
      if (aggregate.isDirty() || force)
      {

        int saveCount=0;
        for (int i=0;i<aggregate.size();i++)
        { 
          BufferTuple buffer=aggregate.get(i);
          if (buffer.isDirty())
          { 
            if (saveChild(buffer))
            { saveCount++;
            }
          }
        }
      
        if (contentRequired && saveCount==0)
        { throw new DataException("Content required for "+getType());
        }
        aggregate.reset();
        writeToModel(aggregate);
      }
      else
      { 
        if (debug)
        { log.fine("BufferAggregate is not dirty");
        }
      }
    }
    
  }  
  
  private boolean saveChild(BufferTuple child)
    throws DataException
  {
    childChannel.push(child);
    try
    {
      
      if (defaultSetters!=null)
      { 
        if (debug)
        { log.fine(toString()+": applying default values to "+child);
        }
        Setter.applyArrayIfNull(defaultSetters);      
      }

      if (fixedSetters!=null)
      {
        if (debug)
        { log.fine(toString()+": applying fixed values to "+child);
        }
        Setter.applyArray(fixedSetters);
      }
      
      child.save();
      return true;
      
    }
    finally
    { childChannel.pop();
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

    
    if (!type.isAggregate())
    { 
      // TODO: Used to traverse fields here, now that's done by the updater
      //   restructure this
      
      throw new BindException
        ("Cannot bind an AggregateEditor to a non-aggregate type "+type);
    }

    childChannel
      =new ThreadLocalChannel<BufferTuple>
        (DataReflector.<BufferTuple>getInstance
          (Type.getBufferType(type.getContentType()))
        ,true
        ,localChannel
        );

    
    
    Focus<?> childFocus=focus.chain(childChannel);
    fixedSetters=Assignment.bindArray(fixedAssignments,childFocus);
    defaultSetters=Assignment.bindArray(defaultAssignments,childFocus);
    newSetters=Assignment.bindArray(newAssignments,childFocus);
    BindingChannel.bindTarget(newBindings,childFocus);
    BindingChannel.bindTarget(preSaveBindings,childFocus);

    initialSetters=Assignment.bindArray(initialAssignments,childFocus);
    publishedSetters=Assignment.bindArray(publishedAssignments,childFocus);
    bindKeys(childFocus);
    return super.bindExports(focus);
  }

}



