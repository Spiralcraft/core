//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.command;

import java.util.List;

import spiralcraft.util.ListMap;


/**
 * <p>Provides a means to execute a sequence of named Commands
 * </p>
 * 
 * @author mike
 * 
 */
public class CallInterface
{

  @SuppressWarnings("rawtypes")
  public ListMap<String,CommandFactory> commands
    =new ListMap<String,CommandFactory>();
  
  @SuppressWarnings("rawtypes")
  public void add(String verb,CommandFactory command)
  { this.commands.add(verb,command);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void execute(Call call)
  { 
    List<CommandFactory> candidates
      =commands.get(call.verb);
    if (candidates==null || candidates.isEmpty())
    { 
      call.setException
        (new RuntimeException("Command '"+call.verb+"' not found"));
      return;
    }
    
    CommandFactory<?,?,?> factory=null;
    for (CommandFactory<?,?,?> candidate: candidates)
    { 
      if (call.context==null 
          || (factory.getContextReflector()==null)
          || (factory.getContextReflector().accepts(call.context.getClass())
             )
         )
      { factory=candidate;
      }
    }
    
    if (factory!=null)
    {
      Command command=factory.command();
      command.setContext(call.context);
      try
      { 
        command.execute();
        call.setResult(command.getResult());
        call.setException(command.getException());
      }
      catch (Exception x)
      { call.setException(x);
      }
    }
    else
    {
      call.setException
        (new RuntimeException("Command '"+call.verb+"("+call.context.getClass()+")' not found"));
    }
      
  }
  
 
}
