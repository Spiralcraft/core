package spiralcraft.loader;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import spiralcraft.command.Command;
import spiralcraft.command.CommandContext;
import spiralcraft.command.ParameterSet;
import spiralcraft.command.ParameterSetDefinition;

public class ModulesCommand
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
    LibraryCatalog catalog=(LibraryCatalog) context.getPeer();
    
    List libraries=catalog.listLibraries();
    
    List messageLines=new ArrayList(libraries.size()+10);
    
    for (Iterator it=libraries.iterator();it.hasNext();)
    { 
      messageLines.add( ((Library) it.next()).path);
    }
    messageLines.add("");
    
    context.handleMessage(messageLines.toArray());
    
    return null;
  }
  
  public String getDescription()
  { return "List of modules in the library";
  }

  public String getName()
  { return "modules";
  }
}
