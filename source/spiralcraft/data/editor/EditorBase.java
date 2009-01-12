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

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandBlock;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.session.DataSession;
import spiralcraft.data.session.Buffer;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;


public abstract class EditorBase<Tbuffer extends Buffer>
{

  private static final ClassLog log
    =ClassLog.getInstance(EditorBase.class);

  
  protected Channel<Buffer> bufferChannel;
  protected Channel<Tuple> source;
  
  private Type<?> type;
  private Channel<DataSession> sessionChannel;

  
  protected Assignment<?>[] fixedAssignments;
  protected Assignment<?>[] initialAssignments;
  protected Assignment<?>[] defaultAssignments;
  protected Assignment<?>[] newAssignments;
  protected Assignment<?>[] publishedAssignments;
  
  protected boolean autoCreate;
  protected boolean retain;
  
  protected boolean debug;
  protected Focus<?> focus;


  
  protected abstract void save(boolean force)
    throws DataException;
  
  public void setSource(Channel<Tuple> source)
  { this.source=source;
  }
  
  protected void setupSession(Focus<?> parentFocus)
  {
    Focus<DataSession> sessionFocus
      =parentFocus.<DataSession>findFocus(DataSession.FOCUS_URI);
    if (sessionFocus!=null)
    { sessionChannel=sessionFocus.getSubject();
    }
  }
  
  protected boolean writeToModel(Tbuffer buffer)
  { return bufferChannel.set(buffer);
  }
  
  protected DataSession getDataSession()
  { return sessionChannel.get();
  }
  
  
  public Type<?> getType()
  { return type;
  }
  
  public void setupType()
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
  
  public boolean isDirty()
  { 
    Buffer buffer=bufferChannel.get();
    return buffer!=null && buffer.isDirty();
  }
                     

  public Command<Tbuffer,Void> revertCommand()
  { 
    return new CommandAdapter<Tbuffer,Void>()
    {
      @Override
      public void run()
      { 
        Buffer buffer=bufferChannel.get();
        if (buffer!=null)
        { buffer.revert();
        }
      }
    };
  }

  public Command<Tbuffer,Void> revertCommand(final Command<?,?> chainedCommand)
  { 
    return new CommandAdapter<Tbuffer,Void>()
    {
      @Override
      public void run()
      { 
        Buffer buffer=bufferChannel.get();
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
  public Command<Tbuffer,Void> saveCommand()
  {     
    return new CommandAdapter<Tbuffer,Void>()
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
  
  @SuppressWarnings("unchecked") // Command block doesn't care about types
  public Command<Tbuffer,Void> saveCommand
    (final List<Command> postSaveCommandList)
  {
    
    return saveCommand(new CommandBlock(postSaveCommandList));
  }
      
  public Command<Tbuffer,Void> saveCommand(final Command<?,?> postSaveCommand)
  { 
    return new CommandAdapter<Tbuffer,Void>()
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
  public Command<Tbuffer,Void> clearCommand()
  {
    return new CommandAdapter<Tbuffer,Void>()
    { 
      @Override
      public void run()
      { writeToModel(null);
      }
    };
  }



  public Command<Tbuffer,Void> newCommand()
  {
    return new CommandAdapter<Tbuffer,Void>()
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
  
  

  


}




