package spiralcraft.exec.test;

import spiralcraft.util.ArrayUtil;

import spiralcraft.exec.Executable;

public class HelloWorld
  implements Executable
{
  private Object _testObject;

  public void execute(String[] args)
  { 
    System.out.println("HelloWorld");
    System.out.println("args: "+ArrayUtil.formatToString(args," ","\""));
    System.out.println("testObject:");
    
    if (_testObject!=null)
    { System.out.println(_testObject.toString());
    }
  }

  public void setTestObject(Object testObject)
  { _testObject=testObject;
  }
}
