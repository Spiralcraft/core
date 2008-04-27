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
package spiralcraft.shell;

import java.util.List;
import java.util.ArrayList;

import spiralcraft.command.Command;
import spiralcraft.command.Commands;
import spiralcraft.command.CommandAdapter;

/**
 * 
 * Provides textual information about the commands available from a Commandable
 * 
 * @author mike
 *
 */
public class HelpCommand
  extends CommandAdapter<Commands<?>,List<String>>
{
  
  private static final ArgumentDefinition _DEFINITION
    =new ArgumentDefinition()
    { 
      {
        addParameter("",1,String.class,false);
      }
    };
    
  public ArgumentSet newParameterSet()
  { return new ArgumentSet(_DEFINITION);
  }
  
  public void run()
  { 
    List<Command<?,?>> commands=null;

    List<String> messageLines=new ArrayList<String>(commands.size()+10);
    messageLines.add("Use help <command> for more information");
    messageLines.add("  about an individual command.");
    messageLines.add("");
    messageLines.add("List of available commands:");
    messageLines.add("");
    
    
    for (Command<?,?> command:commands)
    { 
      if (command!=null);
      messageLines.add("    command: ");
      messageLines.add("description: ");
      messageLines.add("");
    }
    messageLines.add("");
    setResult(messageLines);
  }
  
  public String getDescription()
  { return "Information about available commands";
  }

  public String getName()
  { return "help";
  }
}
