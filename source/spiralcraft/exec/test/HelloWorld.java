package spiralcraft.exec.test;

import spiralcraft.util.ArrayUtil;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

public class HelloWorld
  implements Executable
{
  private Object _testObject;

  public void execute(ExecutionContext context,String[] args)
  { 
    context.out().println("HelloWorld");
    context.out().println("args: "+ArrayUtil.formatToString(args," ","\""));
    context.out().println("testObject:");
    
    if (_testObject!=null)
    { context.out().println(_testObject.toString());
    }
  }

  public void setTestObject(Object testObject)
  { _testObject=testObject;
  }
}
