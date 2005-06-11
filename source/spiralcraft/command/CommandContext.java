//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.command;

import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;

/**
 * Provides to a User Agent a mechanism for discovering,
 *   invoking, and monitoring application functionality via Commands.
 *
 * Components in different horizontal layers of an application 
 *  (eg. User Interface, Application Logic, Storage and Networking, etc.)
 *  may each supply their own CommandContexts. Layers further away from the 
 *  User are chained to a CommandContext that is closer to the User-
 *  ie. the CommandContext for the user interface may control another
 *  CommandContext for the application logic. 
 * 
 * A chain of CommandContexts represents a path to the focus of user (or user 
 *   agent) interaction. The CommandContext at the end of the chain represents
 *   the actual focus. 
 */
public abstract class CommandContext
  implements MessageHandler
{
  
  private final Object _peer;
  private MessageHandler _messageHandler;
  private CommandContext _controllingContext;
  private CommandContext _focus;
  private String _focusName;
  private final LinkedHashMap _contexts=new LinkedHashMap();
 
  /**
   * Construct a CommandContext where commands should operate on the
   *   specified peer object.
   */
  public CommandContext(Object peer)
  { _peer=peer;
  }
  
  public Object getPeer()
  { return _peer;
  }
  
  public void setMessageHandler(MessageHandler messageHandler)
  { _messageHandler=messageHandler;
  }
  
  public void setControllingContext(CommandContext context)
  { 
    _controllingContext=context;
    _messageHandler=context;
  }
  
  /**
   * Add a Context to the list of accessible subcontexts
   */
  public void putContext(String name,CommandContext context)
  { 
    _contexts.put(name,context);
    context.setControllingContext(this);
  }

  /**
   * Return a list of subcontexts
   */
  public List listContexts()
  { 
    LinkedList list=new LinkedList();
    list.addAll(_contexts.keySet());
    return list;
  }
  
  /**
   * Set the current focus to the specified CommandContext. Since this
   *   focus is specified by reference, it will be excluded from the 
   *   focus path
   */
  public void setFocus(CommandContext focus)
  { 
    _focus=focus;
    _focus.setControllingContext(this);
    _focusName=null;
  }
  
  /**
   * Set the current focus to the CommandContext with the specified name.
   */
  public void setFocus(String name)
  { 
    if (name==null)
    { throw new IllegalArgumentException("Focus name cannot be null");
    }
    
    CommandContext focus=(CommandContext) _contexts.get(name);
    if (focus!=null)
    { 
      _focus=focus;
      _focusName=name;
    }
    else
    { throw new IllegalArgumentException("Focus '"+name+"' not found");
    }
  }

  public CommandContext getFocus()
  { return _focus;
  }
  
  /**
   * Return the path to the CommandContext which is currently the focus.
   */
  public String[] getFocusPath()
  { 
    LinkedList path=new LinkedList();
    composeFocusPath(path);
    String[] ret=new String[path.size()];
    path.toArray(ret);
    return ret;
  }
  
  void composeFocusPath(LinkedList previous)
  { 
    if (_focusName!=null)
    { previous.add(_focusName);
    }
    if (_focus!=null)
    { _focus.composeFocusPath(previous);
    }
  }
  
  public void handleMessage(Object[] messageLines)
  { _messageHandler.handleMessage(messageLines);
  }
  
  /**
   * Prepare a new Invocation for the specified command.
   *
   * This method is delegated to the focus.
   */
  public Invocation newInvocation(String commandName)
    throws UnrecognizedCommandException
  { 
    if (_focus!=null)
    { return _focus.newInvocation(commandName);
    }

    Command command=assertCommand(commandName);
    return new Invocation(this,command);
  }
  
  /**
   *@return The command with the specified name, or
   *@throws UnrecognizedCommandException if no command with the specified
   *  name is resolvable from this CommandContext
   */
  private Command assertCommand(String commandName)
    throws UnrecognizedCommandException
  { 
    
    Command command=findCommand(commandName);
    if (command==null)
    { throw new UnrecognizedCommandException(commandName);
    }
    return command;
  }
  
  /**
   *@return The command with the specified name in a controlling CommandContext,
   *   or in this CommandContext if none was found in the controlling 
   *   CommandContext or null if no Command was found.
   */
  Command findCommand(String commandName)
  { 
    Command command=null;
    if (_controllingContext!=null)
    { command=_controllingContext.findCommand(commandName);
    }
    if (command==null)
    { command=getLocalCommand(commandName);
    }
    return command;
  }
  
  /**
   *@return A list of commands available from this context, starting with the
   *  commands from the controlling CommandContext
   */
  public List listCommands()
  { 
    List commands;

    if (_controllingContext!=null)
    { 
      commands=_controllingContext.listCommands();
      commands.addAll(listLocalCommands());
    }
    else
    { commands=listLocalCommands();
    }
    return commands;
  }

  /**
   *@return A list of commands directly associated with this CommandContext
   */
  protected abstract List listLocalCommands();
  
  /**
   *@return The Command with the specified name that is
   *   directly associated with this CommandContext, or null
   *   if no Command with the specified name is directly associated
   *   with this CommandContext
   */
  protected abstract Command getLocalCommand(String commandName);
  
  /**
   *@return A short description of this CommandContext
   */
  public abstract String getDescription();
}
