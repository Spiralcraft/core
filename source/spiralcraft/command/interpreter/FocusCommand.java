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
public class FocusCommand
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
    String name=(String) params.getValue("");
    context.setFocus(name);
    return context.getFocus();
  }
  
  public String getDescription()
  { return "Change the focus of system interaction";
  }

  public String getName()
  { return "focus";
  }
}
