package spiralcraft.loader;

import spiralcraft.main.LoaderDelegate;
import spiralcraft.util.ArrayUtil;

public class Main
  implements  LoaderDelegate
{

  private boolean _verbose;

  public void exec(String[] args)
  {
    if (_verbose)
    { System.err.println("Core loader: Main.exec("+ArrayUtil.formatToString(args,",","\"")+")");
    }
    
    ApplicationManager applicationManager
      =ApplicationManager.getInstance();
  }

  public void setVerbose(boolean val)
  { _verbose=val;
  }
}
