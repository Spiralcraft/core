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
