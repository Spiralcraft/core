package spiralcraft.framework.test;

import spiralcraft.util.ArrayUtil;

import spiralcraft.framework.Executable;

public class HelloWorld
  implements Executable
{

  public void exec(String[] args)
  { 
    System.out.println("HelloWorld");
    System.out.println("args: "+ArrayUtil.formatToString(args," ","\""));
  }
}
