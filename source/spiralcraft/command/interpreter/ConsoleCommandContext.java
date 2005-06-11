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
package spiralcraft.command.interpreter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import spiralcraft.command.CommandContext;
import spiralcraft.command.Command;
import spiralcraft.command.MessageHandler;

/**
 * CommandContext which controls a CommandConsole
 */
public class ConsoleCommandContext
  extends CommandContext
{
  
  private static final LinkedHashMap _COMMANDS=new LinkedHashMap();

  static
  { 
    _COMMANDS.put("help",new HelpCommand());
    _COMMANDS.put("look",new LookCommand());
    _COMMANDS.put("focus",new FocusCommand());
    
  }
  
  public ConsoleCommandContext(CommandConsole peer)
  { super(peer);
  }
  
  protected Command getLocalCommand(String commandName)
  { return (Command) _COMMANDS.get(commandName);
  }

  protected List listLocalCommands()
  { 
    List list=new ArrayList();
    list.addAll(_COMMANDS.values());
    return list;
  }
  
  public String getDescription()
  { return "Command console control";
  }
}
