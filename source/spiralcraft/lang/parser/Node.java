package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticAdapter;

public abstract class Node
{

  public Optic bind(Focus focus)
    throws BindException
  { 
    System.err.println(getClass().getName()+" not implemented");
    return new OpticAdapter();
  }

  public abstract void dumpTree(StringBuffer out,String prefix);

}
