package spiralcraft.loader;

import spiralcraft.main.Spiralcraft;

import spiralcraft.util.ArrayUtil;

import spiralcraft.exec.ApplicationManager;
import spiralcraft.exec.ExecutionTargetException;

/**
 * Main class which creates an application specific ClassLoader to run application functionality
 *   in a managed environment which controls class library resolution and security policy.
 * 
 * Note: This class designed to be loaded in a classloader other than the System classloader,
 *   for example, in a classloader created by the spiralcraft-main package by
 *   invoking the spiralcraft.main.Spiralcraft class (the 'bootstrap loader') from the command line.
 *
 * If the ClassLoader for this class is the System classLoader, an exception will be thrown.
 */
public class Main
{
  static
  {
    if (Main.class.getClassLoader()==ClassLoader.getSystemClassLoader())
    { throw new IllegalStateException("This class cannot be loaded into the System ClassLoader");
    }
  }

  public static void main(String[] args)
    throws Throwable
  {

    if (Spiralcraft.DEBUG)
    { System.err.println("Core loader: Main.main("+ArrayUtil.formatToString(args,",","\"")+")");
    }
     
    ApplicationManager applicationManager
      =ApplicationManager.getInstance();

    try
    { applicationManager.exec(args);
    }
    catch (ExecutionTargetException x)
    { throw x.getTargetException();
    }
    finally
    { ApplicationManager.shutdownInstance();
    }
    
  }

}
