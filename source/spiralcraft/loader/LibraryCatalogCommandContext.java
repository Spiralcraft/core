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
package spiralcraft.loader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import spiralcraft.command.CommandContext;
import spiralcraft.command.Command;

/**
 * CommandContext which controls a CommandConsole
 */
public class LibraryCatalogCommandContext
  extends CommandContext
{
  
  private static final LinkedHashMap<String,Command> _COMMANDS
    =new LinkedHashMap<String,Command>(); 
  
  static
  { 
    _COMMANDS.put("modules",new ModulesCommand());
  }

  public LibraryCatalogCommandContext(LibraryCatalog catalog)
  { super(catalog);
  }
  
  protected Command getLocalCommand(String commandName)
  { return (Command) _COMMANDS.get(commandName);
  }

  protected List<Command> listLocalCommands()
  { 
    List<Command> list=new ArrayList<Command>();
    list.addAll(_COMMANDS.values());
    return list;
  }
  
  public String getDescription()
  { return "Software module library catalog";
  }
}
