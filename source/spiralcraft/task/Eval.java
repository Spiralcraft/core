package spiralcraft.task;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;

public class Eval<Tcontext,Tresult>
  extends Chain<Tcontext,Tresult>
{

  { storeResults=true;
  }

  private Binding<Tresult> x;
  private ThreadLocalChannel<Tresult> resultChannel;
  
  public Eval(Expression<Tresult> x)
  { this.x=new Binding<Tresult>(x);
  }

  public Eval(Expression<Tcontext> contextX,Expression<Tresult> x)
  { 
    this.x=new Binding<Tresult>(x);
    this.contextX=contextX;
  }
  
  public Eval()
  {
  }
  
  public void setX(Binding<Tresult> x)
  { this.x=x;
  }
  
  @Override
  protected Task task()
  { return new EvalTask();
  }

  @Override
  public Focus<?> bindImports(Focus<?> focus)
    throws BindException
  { 
    Focus<?> contextFocus=focus;
    if (contextChannel!=null)
    { contextFocus=focus.chain(contextChannel);
    }
    x.bind(contextFocus);
    resultReflector=x.getReflector();
    resultChannel=new ThreadLocalChannel<Tresult>(resultReflector);
    
    // Don't return the contextFocus because it won't be inside the
    //   closure.
    return focus;
  }
  
  /**
   * 
   */
  @Override
  public Focus<?> bindExports(Focus<?> focus)
  { return focus.chain(resultChannel);
  }
 
  class EvalTask 
    extends ChainTask
  {
    
    @Override
    public void work()
      throws InterruptedException
    {
      
      Tresult result=x.get();
      
      resultChannel.push(result);
      try
      { super.work();
      }
      finally
      { resultChannel.pop();
      }
      addResult(result);
    }
  }
    
}
