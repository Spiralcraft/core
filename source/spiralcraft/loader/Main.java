package spiralcraft.loader;

import spiralcraft.main.LoaderDelegate;
import spiralcraft.util.ArrayUtil;

public class Main
  implements  LoaderDelegate
{

  private boolean _verbose;

  /**
   * Called by the bootstrap loader to handle command-line invocation of the VM
   */
  public int exec(String[] args)
  {
    if (_verbose)
    { System.err.println("Core loader: Main.exec("+ArrayUtil.formatToString(args,",","\"")+")");
    }
    
    ApplicationManager applicationManager
      =ApplicationManager.getInstance();

    ApplicationEnvironment environment
      =applicationManager.createApplicationEnvironment();

    if (args[0].equals("main"))
    { 
      String[] newArgs=new String[args.length-1];
      System.arraycopy(args,1,newArgs,0,newArgs.length);
      return environment.execMain(newArgs);
    }
    else
    { 
      System.err.println("Unrecognized argument "+args[0]);
      return 1;
    }
    
  }

  public void setVerbose(boolean val)
  { _verbose=val;
  }
}
