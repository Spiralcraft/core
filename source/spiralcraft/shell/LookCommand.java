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
package spiralcraft.shell;


import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandMenu;

/**
 * Provides information about the current Commandable
 */
public class LookCommand
  extends CommandAdapter<CommandMenu,String>
{
  private static final ArgumentDefinition _DEFINITION
    =new ArgumentDefinition()
    { 
      {
        addParameter("",1,String.class,false);
      }
    };
  
  public ArgumentSet newParameterSet()
  { return new ArgumentSet(_DEFINITION);
  }
  
  @Override
  public void run()
  { 

  }
  
  public String getDescription()
  { return "Information about a command context";
  }

  public String getName()
  { return "look";
  }
}
