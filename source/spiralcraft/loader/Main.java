package spiralcraft.loader;

import spiralcraft.main.LoaderDelegate;
import spiralcraft.main.Spiralcraft;

import spiralcraft.util.ArrayUtil;

/**
 * Native OS shell interface to Spiralcraft managed code. Handles external command line
 *   arguments passed to the VM by resolving command aliases and passing the translated
 *   command line to a new ApplicationEnvironment for execution.
 *
 * Aliases are used to simplify invoking Spiralcraft managed code from a native OS command
 *   line. An alias maps the first parameter of a command to a fragment of a command line
 *   which is substituted for the alias. 
 *
 * Aliases are the primary mechanism to expose Spiralcraft managed functionality to the native
 *   OS command line. As such, a flexible yet predictable mechanism for resolving aliases is
 *   provided.
 *
 * Aliases fall into 4 categories: built-in aliases,  module aliases, host aliases and user aliases.
 *
 * Built-in aliases simply provide a means for querying and setting up other aliases.
 *
 * Module aliases are provided by Spiralcraft modules to simplify invoking application
 *   functionality contained in Spiralcraft managed modules.
 *
 * Host aliases provide a means for system administrators to run functionality localized to a specific host.
 *
 * User aliases allow users to simplify commonly invoked functionality.
 */
public class Main
  implements  LoaderDelegate
{

  /**
   * Called by the bootstrap loader to handle command-line invocation of the VM
   * 
   * Translates aliases and delegates execution to a new application manager
   */
  public int exec(String[] args)
  {
    if (Spiralcraft.DEBUG)
    { System.err.println("Core loader: Main.exec("+ArrayUtil.formatToString(args,",","\"")+")");
    }
    
    ApplicationManager applicationManager
      =ApplicationManager.getInstance();

    ApplicationEnvironment environment
      =applicationManager.createApplicationEnvironment();

    args=resolveAliases(args);
    return environment.exec(args);
  }


  public String[] resolveAliases(String[] args)
  { return args;
  }
}
