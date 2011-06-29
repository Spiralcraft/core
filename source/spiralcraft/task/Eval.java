package spiralcraft.task;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * Evaluates an expression and returns the result
 * 
 * @author mike
 *
 * @param <Tcontext>
 * @param <Tresult>
 */
public class Eval<Tcontext,Tresult>
  extends Chain<Tcontext,Tresult>
{

  { 
    storeResults=true;
    // importContext=true;
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

  
  /**
   * 
   */
  @Override
  protected void bindInContext(Focus<?> focus)
    throws BindException
  { 
    x.bind(focus);
    resultReflector=x.getReflector();
    resultChannel=new ThreadLocalChannel<Tresult>(resultReflector);
    
  }
  
  /**
   * 
   */
  @Override
  public Focus<?> bindExports(Focus<?> focus)
    throws BindException
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
