package spiralcraft.command.interpreter;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import spiralcraft.command.Command;
import spiralcraft.command.CommandContext;
import spiralcraft.command.ParameterSet;
import spiralcraft.command.ParameterSetDefinition;

/**
 * Provides information about the current context or a subcontext
 */
public class LookCommand
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
    CommandContext targetContext=context;
    
    List contexts=targetContext.listContexts();

    List messageLines=new ArrayList(contexts.size()+10);
    messageLines.add(targetContext.getDescription());
    
    for (Iterator it=contexts.iterator();it.hasNext();)
    { messageLines.add("  "+it.next());
    }
    
    context.handleMessage(messageLines.toArray());
    
    return targetContext;
  }
  
  public String getDescription()
  { return "Information about a command context";
  }

  public String getName()
  { return "look";
  }
}
