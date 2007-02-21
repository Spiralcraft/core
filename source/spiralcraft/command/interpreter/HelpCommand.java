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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import spiralcraft.command.Command;
import spiralcraft.command.CommandContext;
import spiralcraft.command.ParameterSet;
import spiralcraft.command.ParameterSetDefinition;

public class HelpCommand
  implements Command
{
  
  private static final ParameterSetDefinition _DEFINITION
    =new ParameterSetDefinition()
    { 
      {
        addParameter("",1,String.class,false);
      }
    };
    
  public ParameterSet newParameterSet()
  { return new ParameterSet(_DEFINITION);
  }
  
  public Object execute(CommandContext context,ParameterSet params)
  { 
    List<Command> commands=context.listCommands();

    List<String> messageLines=new ArrayList<String>(commands.size()+10);
    messageLines.add("Use help <command> for more information");
    messageLines.add("  about an individual command.");
    messageLines.add("");
    messageLines.add("List of available commands:");
    messageLines.add("");
    
    
    for (Iterator it=commands.iterator();it.hasNext();)
    { 
      Command command=(Command) it.next();
      messageLines.add("    command: "+command.getName());
      messageLines.add("description: "+command.getDescription());
      messageLines.add("");
    }
    messageLines.add("");
    
    context.handleMessage(messageLines.toArray());
    
    return commands;
  }
  
  public String getDescription()
  { return "Information about available commands";
  }

  public String getName()
  { return "help";
  }
}
