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

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.string.StringUtil;

import spiralcraft.lang.Focus;

import spiralcraft.command.Command;


/**
 * <p>Abstract mechanism to implement a stateful command line interface
 * </p>
 * 
 * <p>The subclass is responsible for translating input into lines, and for
 *   rendering any output that may be generated.
 * </p>
 * 
 * <p>Command lines are tokenized using the tokenizeCommandLine method
 *   in the spiralcraft.util.StringUtil class.
 * </p>
 * 
 * <p>Once tokenized, the command line is interpreted as follows:
 * </p>
 * 
 * <h3><i> XXX THE FOLLOWING IS IN FLUX XXX</i></h3>
 * * The first token is always a literal command name, to be resolved
 *   within the XXX DEFINE COMMAND RESOLUTION  XXX
 *
 * For each successive token in the command line:
 * 
 * * XXX 
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
  implements MessageHandler
{

  private Focus<?> focus;
  private String[] focusPath;
  
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
        Command<?,?,?> command
          =newCommand(tokens);
        
        command.execute();
        
        Exception x;
        if ((x=command.getException())!=null)
        { 
          writeMessage(x.toString());
          StackTraceElement[] trace=x.getStackTrace();
          writeMessage(trace[0].toString());
        }
      }
      catch (UnrecognizedCommandException x)
      { writeMessage(x.toString());
      }
    }
  }

  public void setFocus(Focus<?> focus)
  { this.focus=focus;
  }
  
  /**
   * @param focusExpression
   */
  public void changeFocus(String focusExpression)
  {
    if (focus!=null) {}
  }
  
  public String getFocusPathString()
  { return ArrayUtil.format(focusPath,":","");
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
  
  /**
   * 
   * @throws UnrecognizedCommandException
   */
  private Command<?,?,?> newCommand(String[] tokens)
    throws UnrecognizedCommandException
  {
    String commandName=tokens[0];
    if (commandName!=null) {}
    
    // XXX The following is stubbed. Now that we have better CommandFactories
    //  and the notion of command context, we can resolve argument definitions
    //  via the command context
    Command<?,?,?> command=null; // XXX Resolve command here
    ArgumentSet parameterSet
      =new ArgumentSet(new ArgumentDefinition()); // XXX Resolve command argument set
    
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
          // String valueString=tokens[i];
          parameterSet.addValue(parameterName,tokens[i]);
        }
      }
      else
      { 
        if (!parameterSet.getDefinition().isNameValid(""))
        { throw new IllegalArgumentException("This command does not accept unnamed parameters");
        }
        // String valueString=tokens[i];
        parameterSet.addValue("",tokens[i]);
      }
    }
    return command;
  }
  
  @Override
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
