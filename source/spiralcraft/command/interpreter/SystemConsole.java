package spiralcraft.command.interpreter;

import spiralcraft.util.ArrayUtil;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;

import spiralcraft.command.CommandContext;

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
