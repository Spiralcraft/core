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
    List commands=context.listCommands();

    List messageLines=new ArrayList(commands.size()+10);
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
