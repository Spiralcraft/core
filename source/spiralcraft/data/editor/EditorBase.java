//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.editor;


import java.util.List;

import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandBlock;
import spiralcraft.command.CommandFactory;
import spiralcraft.common.ContextualException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Key;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.core.RelativeField;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.session.BufferChannel;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.types.meta.MetadataType;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Context;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;


public abstract class EditorBase<Tbuffer extends Buffer>
  implements Context
{

  private static final ClassLog log
    =ClassLog.getInstance(EditorBase.class);

  
  private Channel<Tbuffer> bufferChannel;
  
  protected ThreadLocalChannel<Tbuffer> localChannel;
  
  protected Channel<? extends DataComposite> source;
  
  private Type<?> type;
  private Channel<DataSession> sessionChannel;

  
  protected Assignment<?>[] fixedAssignments;
  protected Assignment<?>[] initialAssignments;
  protected Assignment<?>[] defaultAssignments;
  protected Assignment<?>[] newAssignments;
  protected Assignment<?>[] publishedAssignments;
  
  protected Binding<?> onCreate;
  protected Binding<?> onInit;
  protected Binding<?> preSave;
  protected Binding<?> afterSave;

  private Channel<Tuple> parentChannel;
  private Channel<Tuple> parentKeyChannel;
  private Channel<Tuple> localKeyChannel;
  
  protected boolean autoKey;
  protected boolean autoCreate;
  protected boolean retain;
  
  protected boolean debug;
  protected Focus<?> focus;


  public final CommandFactory<Tbuffer,Void,Void> create
    =new AbstractCommandFactory<Tbuffer,Void,Void>()
  {

    @Override
    public Command<Tbuffer, Void, Void> command()
    { return newCommand();
    }
  };
  
  public final CommandFactory<Tbuffer,Void,Void> save
    =new AbstractCommandFactory<Tbuffer,Void,Void>()
  {

    @Override
    public Command<Tbuffer, Void, Void> command()
    { return saveCommand();
    }
  };
  
  public void setSource(Channel<? extends DataComposite> source)
  { this.source=source;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  /**
   * The Editor will create a new Buffer if the source provides a null
   *   original value and buffers are not being retained
   */
  public void setAutoCreate(boolean val)
  { autoCreate=val;
    
  }
  
  /**
   * Retain any original value if the source provides a null value
   */
  public void setRetain(boolean val)
  { retain=val;
  }
  
  /**
   * New Assignments get executed when a buffer is new (ie. has no original) 
   *   and is not yet dirty.
   * 
   * @param assignments
   */
  public void setNewAssignments(Assignment<?>[] assignments)
  { newAssignments=assignments;
  }
  
  /**
   * Initial Assignments get executed when a buffer is not yet dirty.
   * 
   * @param assignments
   */
  public void setInitialAssignments(Assignment<?>[] assignments)
  { initialAssignments=assignments;
  }

  /**
   * <p>Default Assignments get executed immediately before storing, if
   *   the Tuple is dirty already, and the existing field data is null.
   *   
   * @param assignments
   */
  public void setDefaultAssignments(Assignment<?>[] assignments)
  { defaultAssignments=assignments;
  }

  /**
   * <p>Fixed Assignments get executed immediately before storing, if the
   *   Tuple is dirty already, overwriting any existing field data.
   * </p>
   * 
   * @param assignments
   */
  public void setFixedAssignments(Assignment<?>[] assignments)
  { fixedAssignments=assignments;
  }

  /**
   * <p>Published assignments get executed on the Prepare message, which 
   *   occurs before rendering. This permits publishing of data to
   *   containing contexts. 
   * </p>
   * 
   * @param assignments
   */
  public void setPublishedAssignments(Assignment<?>[] assignments)
  { publishedAssignments=assignments;
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
  
  public void setAfterSave(Binding<?> afterSave)
  { this.afterSave=afterSave;
  }

  public void setPreSave(Binding<?> preSave)
  { this.preSave=preSave;
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void initBuffer()
    throws DataException
  {
    if (autoCreate && localChannel.get()==null)
    { 
      Command<?,?,?> newCommand=newCommand();
      newCommand.execute();
      if (newCommand.getException()!=null)
      { 
        throw new DataException
        ("Error initializing buffer",newCommand.getException());
      }
    }
    ensureInitialized();
  }
  
  /**
   * <p>Bind the Editor into the focus chain
   * </p>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Focus<?> bind
    (Focus<?> parentFocus)
      throws ContextualException
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
      { bufferChannel=(Channel<Tbuffer>) source;
      }
      else
      {
        if (debug)
        { log.fine("Creating BufferChannel for "+source.getReflector());
        }
        bufferChannel=bindBuffer(parentFocus);
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
    
    setupSession(parentFocus);
    localChannel=new ThreadLocalChannel<Tbuffer>(bufferChannel,true);
    focus=parentFocus.chain(localChannel);
    bindExports(focus);
    return focus;
  }

  

  
  @Override
  public void push()
  { 
    localChannel.push();
  }
  

  
  
  public Tbuffer getBuffer()
  { return localChannel.get();
  }

  @Override
  public void pop()
  { localChannel.pop();
  }
  

  
  public boolean isDirty()
  { 
    Tbuffer buffer=localChannel.get();
    return buffer!=null && buffer.isDirty();
  }
                     

  public Command<Tbuffer,Void,Void> revertCommand()
  { 
    return new CommandAdapter<Tbuffer,Void,Void>()
    {
      @Override
      public void run()
      { 
        Tbuffer buffer=localChannel.get();
        if (buffer!=null)
        { buffer.revert();
        }
      }
    };
  }

  public Command<Tbuffer,Void,Void> revertCommand
    (final Command<?,?,?> chainedCommand)
  { 
    return new CommandAdapter<Tbuffer,Void,Void>()
    {
      @Override
      public void run()
      { 
        Tbuffer buffer=localChannel.get();
        if (buffer!=null)
        { 
          buffer.revert();
          chainedCommand.execute();
        }
      }
    };
  }

  /**
   * A Command that saves the buffer tree starting with this buffer.
   * 
   * @return the Save command
   */
  public Command<Tbuffer,Void,Void> saveCommand()
  {     
    return new CommandAdapter<Tbuffer,Void,Void>()
    {
      @Override
      public void run()
      { 
        try
        { save(false);
        }
        catch (DataException x)
        { setException(x);
        }
        
      }
    };
  }
  
  @SuppressWarnings({ "rawtypes" }) // Command block doesn't care about types
  public Command<Tbuffer,Void,Void> saveCommand
    (final List<Command> postSaveCommandList)
  {
    
    return saveCommand(new CommandBlock(postSaveCommandList));
  }
      
  public Command<Tbuffer,Void,Void> saveCommand(final Command<?,?,?> postSaveCommand)
  { 
    return new CommandAdapter<Tbuffer,Void,Void>()
    { 
      @Override
      public void run()
      { 
        try
        {
          save(false);
          postSaveCommand.execute();
        }
        catch (DataException x)
        { setException(x);
        }
          
      }
    };   
  }
  
  /**
   * <p>Clears the Editor to accomodate a new Tuple
   * </p>
   * 
   * @return A new Command
   */
  public Command<Tbuffer,Void,Void> clearCommand()
  {
    return new CommandAdapter<Tbuffer,Void,Void>()
    { 
      @Override
      public void run()
      { writeToModel(null);
      }
    };
  }



  
  public Command<Tbuffer,Void,Void> newCommand()
  {
    return new CommandAdapter<Tbuffer,Void,Void>()
    { 
      @Override
      public void run()
      { 
        try
        { newBuffer();
        }
        catch (DataException x)
        { setException(x);
        }
      }
    };
  }
  
  
  
  
  /**
   * Create a new buffer
   */
  protected void newBuffer()
   throws DataException
  { 
    if (debug)
    { log.fine("New buffer for "+type.getURI());
    }
    writeToModel(sessionChannel.get().<Tbuffer>newBuffer(type));
  }
  
  

  protected void setupSession(Focus<?> parentFocus)
  { sessionChannel=DataSession.findChannel(parentFocus);
  }
  
  protected boolean writeToModel(Tbuffer buffer)
  { 
    localChannel.set(buffer);
    return bufferChannel.set(buffer);
  }
  
  protected DataSession getDataSession()
  { return sessionChannel.get();
  }
  
  protected void applyKeyValues()
  {
    if (parentKeyChannel!=null && localKeyChannel!=null)
    { 
      localKeyChannel.set(parentKeyChannel.get());
    }
  }
  
  protected void setupType()
    throws BindException
  {
    Type<?> newType
      =((DataReflector<?>) bufferChannel.getReflector()).getType();

    if (type!=null)
    { 
      // Subtype expressed in config
      if (!newType.isAssignableFrom(type))
      { 
        throw new BindException
          ("target type "+newType.getURI()
          +" is not assignable from configured type "+type.getURI()
          );
      
      }
    }
    else
    { type=newType;
    }
    
  }



  

  protected Channel<Tbuffer> bindBuffer(Focus<?> focus)
    throws BindException
  {
    return new BufferChannel<Tbuffer>
      (focus
      ,source
      );
  }
  
  protected Focus<?> bindExports(Focus<?> focus)
    throws ContextualException
  { return focus;
  }
  

  @SuppressWarnings("unchecked")
  protected void bindKeys(Focus<?> focus)
    throws ContextualException
  {
    if (!autoKey)
    { return;
    }
    Channel<Tuple> source=(Channel<Tuple>) focus.getSubject();
    // Get information about the relationship to auto-bind key values
    Channel<Field<?>> fieldChannel
      =source.<Field<?>>resolveMeta(focus,MetadataType.RELATIVE_FIELD.uri);
    if (fieldChannel!=null)
    { 
      Field<?> field=fieldChannel.get();
      if (debug)
      { log.debug("Got field metadata for "+field.getURI());
      }
      if (field instanceof RelativeField)
      {
        if (debug)
        { log.debug("Got key metadata for "+field.getURI());
        }
        RelativeField<?> rfield=(RelativeField<?>) field;
        parentChannel=LangUtil.findChannel
          (DataReflector.getInstance
            (rfield.getScheme().getType())
              .getTypeURI()
          ,focus
          );

        Key<Tuple> parentKey=(Key<Tuple>) rfield.getKey();
        parentKeyChannel
          =parentKey.bindChannel(parentChannel,focus,null);
            
        Type<?> type=((DataReflector<?>) source.getReflector()).getType();
        String[] localFieldNames=parentKey.getImportedKey().getFieldNames();
        Key<Tuple> localKey=(Key<Tuple>) type.findKey(localFieldNames);
        if (localKey==null)
        { 
          throw new ContextualException
            ("Could not find key ["+ArrayUtil.format(localFieldNames,",","")
             +"] in "+type.getURI()
            );
        }
        localKeyChannel=localKey.bindChannel(source,focus,null);
      }
      else
      {
        if (debug)
        { log.debug("No key metadata for "+field.getURI()+"("+field.getClass()+")");
        }
      }
    }    
    else
    {
      if (debug)
      { log.debug("No field metadata: "+source.trace(null).toString());
      }

    }
  }

  protected abstract void ensureInitialized();


  public abstract void save(boolean force)
    throws DataException;
}




