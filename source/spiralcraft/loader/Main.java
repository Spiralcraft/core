package spiralcraft.loader;

import spiralcraft.main.Spiralcraft;

import spiralcraft.util.ArrayUtil;

import spiralcraft.exec.ApplicationManager;
import spiralcraft.exec.ExecutionTargetException;

import spiralcraft.security.SystemSecurityManager;

import spiralcraft.command.interpreter.SystemConsole;

/**
 * Standard entry point for launching managed applications. 
 *
 * This class sets up a "global" ApplicationManager and gives it control
 *   of the process.
 *
 * Note: In order for the ApplicationManager to run properly, this class
 *   must NOT be loaded into the system classloader (ie. called directly from
 *   the OS), as this will prevent applications which use different versions
 *   of the Spiralcraft core module and other shared modules from loading
 *   classes. 
 *  
 *   This class designed to be loaded in a classloader other than the System
 *   classloader, for example, in the classloader created by the
 *   spiralcraft-main package by invoking the spiralcraft.main.Spiralcraft
 *   class (the 'bootstrap loader') from the command line.
 *
 *   If the ClassLoader for this class is the System classLoader, an exception
 *   will be thrown.
 */
public class Main
{
  
  static
  {
    if (Main.class.getClassLoader()==ClassLoader.getSystemClassLoader())
    { throw new IllegalStateException("This class cannot be loaded into the System ClassLoader");
    }
    // System.setSecurityManager(new SystemSecurityManager());
  }

  public static void main(String[] args)
    throws Throwable
  {

    if (Spiralcraft.DEBUG)
    { System.err.println("spiralcraft.loader.Main.main("+ArrayUtil.formatToString(args,",","\"")+")");
    }
     
    ApplicationManager applicationManager
      =ApplicationManager.getInstance();

    if (Spiralcraft.DEBUG)
    { applicationManager.setDebug(true);
    }
    
    try
    {
      if (args.length>0)
      { 
        // Execute a single command, then exit
        applicationManager.exec(args);
      }
      else
      { 
        // Execute the command interpreter- the 
        //   initial context assumes a user in a file system directory
        SystemConsole console=new SystemConsole();
        console.setFocus(applicationManager.newCommandContext());
        console.run();

        //        System.err.println(new Usage().toString());
      }
    }
    catch (ExecutionTargetException x)
    { throw x.getTargetException();
    }
    finally
    { ApplicationManager.shutdownInstance();
    }
    
  }

}
