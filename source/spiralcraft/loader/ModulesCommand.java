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
