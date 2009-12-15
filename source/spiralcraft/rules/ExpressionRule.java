package spiralcraft.rules;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

/**
 * <p>A Rule which is Violated when a boolean expression fails when evaluated
 *   against the subject value.
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
  
  public ExpressionRule()
  { }
  
  public ExpressionRule(String expression)
  { setExpression(Expression.<Boolean>create(expression));
  }
  
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

  @SuppressWarnings("unchecked")
  @Override
  public Channel<Violation<T>> bindChannel
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?>[] args
    )
    
    throws BindException
  { 
    if (focus.getSubject()!=source)
    { focus=focus.chain(source);
    }
    return new ExpressionRuleChannel((Focus<T>) focus);
  }

  class ExpressionRuleChannel
    extends RuleChannel<T>
  {
    private final Channel<Boolean> channel;
    
    public ExpressionRuleChannel(Focus<T> focus)
      throws BindException
    { channel=focus.bind(expression);
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
    
  }
  
}

