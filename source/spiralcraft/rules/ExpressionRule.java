package spiralcraft.rules;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.text.MessageFormat;

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
  private MessageFormat messageFormat;
  
  public ExpressionRule()
  { }
  
  public ExpressionRule(String expression)
  { setExpression(Expression.<Boolean>create(expression));
  }
  
  public void setExpression(Expression<Boolean> expression)
  { this.expression=expression;
  }
  
  public void setMessageFormat(MessageFormat messageFormat)
  { this.messageFormat=messageFormat;
  }
  
  public void setX(Expression<Boolean> expression)
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
    if (!focus.isContext(source))
    { focus=focus.chain(source);
    }
    return new ExpressionRuleChannel((Focus<T>) focus);
  }

  class ExpressionRuleChannel
    extends RuleChannel<T>
  {
    private final Channel<Boolean> channel;
    private MessageFormat messageFormat;
    
    public ExpressionRuleChannel(Focus<T> focus)
      throws BindException
    { 
      channel=focus.bind(expression);
      if (ExpressionRule.this.messageFormat!=null)
      { 
        try
        { 
          messageFormat=ExpressionRule.this.messageFormat.clone();
          messageFormat.bind(focus);
        }
        catch (CloneNotSupportedException x)
        { throw new BindException("Error cloning rule message format",x);
        }
        catch (ContextualException x)
        { throw new BindException("Error parsing rule message format",x);
        }
        
      }
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
      { 
        return new Violation<T>
          (ExpressionRule.this
          ,messageFormat!=null
            ?messageFormat.render()
            :ExpressionRule.this.getMessage()
          );
      }
    }
    
  }
  
}

