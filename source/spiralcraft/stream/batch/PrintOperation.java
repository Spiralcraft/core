package spiralcraft.stream.batch;

import spiralcraft.util.Arguments;

import spiralcraft.stream.Resource;

import spiralcraft.exec.ExecutionContext;

public class PrintOperation
  implements Operation
{

  private Operation _nextOperation;
  
  public void setNextOperation(Operation next)
  { _nextOperation=next;
  }
  
  public void invoke(Resource resource)
    throws OperationException
  { 
    ExecutionContext.getInstance().out().println(resource.getURI());
    if (_nextOperation!=null)
    { _nextOperation.invoke(resource);
    }
  }
  
  public boolean processOption(Arguments args,String option)
  { return false;
  }
  
  public boolean processArgument(Arguments args,String argument)
  { return false;
  }
}
