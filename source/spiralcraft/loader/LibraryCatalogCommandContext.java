package spiralcraft.loader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import spiralcraft.command.CommandContext;
import spiralcraft.command.Command;
import spiralcraft.command.MessageHandler;

/**
 * CommandContext which controls a CommandConsole
 */
public class LibraryCatalogCommandContext
  extends CommandContext
{
  
  private static final LinkedHashMap _COMMANDS=new LinkedHashMap(); 
  
  static
  { _COMMANDS.put("modules",new ModulesCommand());
  }

  public LibraryCatalogCommandContext(LibraryCatalog catalog)
  { super(catalog);
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
  { return "Software module library catalog";
  }
}
