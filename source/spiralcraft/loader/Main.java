package spiralcraft.loader;

import spiralcraft.main.LoaderDelegate;

public class Main
  implements LoaderDelegate
{

  private boolean _verbose;

  public void main(String[] args)
  {
    if (_verbose)
    { System.err.println("Core loader entered main");
    }
  }

  public void setVerbose(boolean val)
  { _verbose=val;
  }
}
