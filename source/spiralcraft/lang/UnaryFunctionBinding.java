package spiralcraft.lang;

import spiralcraft.common.ContextualException;
import spiralcraft.common.callable.UnaryFunction;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;


public class UnaryFunctionBinding<I,R,X extends Exception>
  implements Contextual,UnaryFunction<I,R,X>
{
  
  private ThreadLocalChannel<I> inputChannel;
  private Reflector<I> inputReflector;
  private Binding<R> result;

  public UnaryFunctionBinding(String expression) 
    throws ParseException
  { result=new Binding<R>(expression);
  }
  
  public void setInputReflector(Reflector<I> inputReflector)
  { this.inputReflector=inputReflector;
  }
  
  public void setInputClass(Class<I> inputClass)
  { this.inputReflector=BeanReflector.<I>getInstance(inputClass);
  }
  
  @Override
  public Focus<?> bind(Focus<?> chain)
    throws ContextualException
  { 
    inputChannel=new ThreadLocalChannel<I>(inputReflector);
    result.bind(chain.chain(inputChannel));
    return chain;
  }
  
  /**
   * 
   */
  @Override
  public R evaluate(I input)
    throws X
  {
    inputChannel.push(input);
    try
    { return result.get();
    }
    finally
    { inputChannel.pop();
    }
  }

}
