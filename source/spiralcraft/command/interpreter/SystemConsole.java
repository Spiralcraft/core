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


import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;


/**
 * Interfaces the standard IO mechanism (System.in,System.out,System.err) to
 *   the command system.
 */
public class SystemConsole
  extends CommandConsole
{
  public void run()
  {
    LineNumberReader reader
      =new LineNumberReader(new InputStreamReader(System.in));

    writeMessage("");
    writeMessage("[ Spiralcraft System Console ]");
    
    writePrompt();
    try
    {
      while (true)
      { 
        String line=reader.readLine();
        if (line==null)
        { break;
        }
        acceptCommand(line);
        writePrompt();        
      }
    }
    catch (IOException x)
    { x.printStackTrace();
    }
  }
  
  protected void writeMessage(CharSequence output)
  { 
    System.out.println();
    System.out.print(output.toString());
    System.out.flush();
  }
}
