package spiralcraft.exec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import spiralcraft.command.CommandContext;
import spiralcraft.command.Command;
import spiralcraft.command.MessageHandler;

/**
 * CommandContext which controls a CommandConsole
 */
public class ApplicationManagerCommandContext
  extends CommandContext
{
  
  private static final LinkedHashMap _COMMANDS=new LinkedHashMap();
  
  static
  { 
  }

  public ApplicationManagerCommandContext(ApplicationManager manager)
  { 
    super(manager);
    putContext("catalog"
              ,manager.getLibraryCatalog().newCommandContext()
              );
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
  { return "Application manager";
  }
}
