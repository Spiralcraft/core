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
