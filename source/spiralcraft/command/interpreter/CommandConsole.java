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

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.StringUtil;

import spiralcraft.command.CommandContext;
import spiralcraft.command.Invocation;
import spiralcraft.command.UnrecognizedCommandException;
import spiralcraft.command.MessageHandler;
import spiralcraft.command.ParameterSet;

import spiralcraft.places.UserAgent;

/**
 * Abstract mechanism to implement an user friendly input driven command
 *   interface using simple line based textual I/O and a TCL based syntax.
 *
 * The subclass is responsible for translating input into lines, and for
 *   rendering any output that may be generated.
 *
 * Command lines are tokenized using the tokenizeCommandLine method
 *   in the spiralcraft.util.StringUtil class.
 *
 * Once tokenized, the command line is interpreted as follows:
 *
 * * The first token is always a literal command name, to be resolved
 *   within the current CommandContext.
 *
 * For each successive token in the command line:
 *
 * * If the token starts with an '-' it is treated as a parameter name and
 *   the next token will be evaluated as a 'value token'.
 *
 * * A 'value token', if preceeded by a '$', is treated as an Expression
 *   to be resolved against the  XXX (the user context? Place to store
 *   intermediate results?)
 *
 * * If the token starts with a '>' or '>>' followed by a name, it represents 
 *   a variable to be assigned the result of the command invocation, and must
 *   be the last token on the command line. A '>' indicates that the variable
 *   contents will be overwritten with the command result and and the '>>'
 *   indicates that the variable contents will be appended with the command
 *   result (in which case the variable will be of an array type).
 *
 * * Tokens that do not correspond to any of the above special cases are
 *   considered to be 'value tokens' which represent unnamed parameter
 *   values.
 *
 *
 */
public abstract class CommandConsole
  implements MessageHandler,UserAgent
{
  private final CommandContext _context
    =new ConsoleCommandContext(this);
  { _context.setMessageHandler(this);
  }

  
  /**
   * Process the Command input from the User Agent. Each line of
   *   input will be treated as a single Command.
   */
  public final void acceptCommand(CharSequence input)
  { 
    String[] tokens=StringUtil.tokenizeCommandLine(input.toString());
    if (tokens.length>0)
    {
      try
      {
        Invocation invocation
          =newInvocation(tokens);
        
        invocation.invoke();
        
        if (invocation.failed())
        { 
          writeMessage(invocation.getThrowable().toString());
          StackTraceElement[] trace=invocation.getThrowable().getStackTrace();
          writeMessage(trace[0].toString());
        }
      }
      catch (UnrecognizedCommandException x)
      { writeMessage(x.toString());
      }
    }
  }

  public void setFocus(CommandContext focus)
  { _context.setFocus(focus);
  }
  
  public String getFocusPathString()
  { 
    String[] focusPath=
      _context.getFocusPath();
    return ArrayUtil.formatToString(focusPath,":","");
  }
  
  public String getUserID()
  { return System.getProperty("user.name")+"@localhost";
  }
  

  public final void writePrompt()
  { 
    writeMessage("");
    writeMessage(getUserID()+":"+getFocusPathString());
    writeMessage("> ");
  }
  
  private Invocation newInvocation(String[] tokens)
    throws UnrecognizedCommandException
  {
    String commandName=tokens[0];
    Invocation invocation=_context.newInvocation(commandName);
    ParameterSet parameterSet=invocation.getParameterSet();
    
    for (int i=0;i<tokens.length;i++)
    { 
      if (tokens[i].startsWith("-"))
      { 
        String parameterName=tokens[i].substring(1);
        if (!parameterSet.getDefinition().isNameValid(parameterName))
        { throw new IllegalArgumentException("Unknown parameter '"+parameterName+"'");
        }
        if (parameterSet.getDefinition().getCount(parameterName)>0)
        { 
          i++;
          if (i==tokens.length)
          { throw new IllegalArgumentException("Parameter '"+parameterName+"' requires a value");
          }
          String valueString=tokens[i];
          parameterSet.addValue(parameterName,tokens[i]);
        }
      }
      else
      { 
        if (!parameterSet.getDefinition().isNameValid(""))
        { throw new IllegalArgumentException("This command does not accept unnamed parameters");
        }
        String valueString=tokens[i];
        parameterSet.addValue("",tokens[i]);
      }
    }
    return invocation;
  }
  
  public void handleMessage(Object[] messageLines)
  { 
    for (int i=0;i<messageLines.length;i++)
    { writeMessage(messageLines[i].toString());
    }
  }
  
  /**
   * Write messages (including prompts) back to the User Agent. 
   * Each line of output represents a single message.
   */
  protected abstract void writeMessage(CharSequence output);
}
