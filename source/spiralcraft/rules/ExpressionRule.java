package spiralcraft.rules;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.BeanReflector;

/**
 * <p>A Rule which is Violated when a boolean expression fails.
 * </p>
 * 
 * @author mike
 *
 * @param <C>
 * @param <T>
 */
public class ExpressionRule<C,T>
  extends AbstractRule<C,T>
{

  private Expression<Boolean> expression;
  private boolean ignoreNull;
  
  public void setExpression(Expression<Boolean> expression)
  { this.expression=expression;
  }
  
  /**
   * <p>Indicate that a null result will not trigger a violation
   * </p>
   * 
   * @param ignoreNull
   */
  public void setIgnoreNull(boolean ignoreNull)
  { this.ignoreNull=ignoreNull;
  }

  @Override
  public Channel<Violation<T>> bindChannel(
    Focus<T> focus)
    throws BindException
  { return new RuleChannel(focus);
  }

  class RuleChannel
    extends AbstractChannel<Violation<T>>
  {

    private final Channel<Boolean> channel;
    
    public RuleChannel(Focus<T> focus)
      throws BindException
    { 
      super(BeanReflector.<Violation<T>>getInstance(Violation.class));
      channel=focus.bind(expression);
    }
    
    @Override
    protected Violation<T> retrieve()
    {
      Boolean result=channel.get();
      if (result!=null && result)
      { return null;
      }
      else if (result==null && ignoreNull)
      { return null;
      }
      else
      { return new Violation<T>(ExpressionRule.this,getMessage());
      }
    }

    @Override
    protected boolean store(
      Violation<T> val)
      throws AccessException
    { return false;
    }
    
  }
  
}

