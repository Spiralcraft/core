package spiralcraft.rules;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

import java.util.regex.Pattern;

/**
 * <p>A Rule which is Violated when the supplied value does not match
 *   a specified regex pattern.
 * </p>
 * 
 * @author mike
 *
 * @param <C>
 * @param <T>
 */
public class PatternRule<C,T>
  extends AbstractRule<C,T>
{

  private Pattern pattern;
  private boolean ignoreNull;
  
  public PatternRule()
  { }
  
  public PatternRule(String pattern)
  { setRegex(pattern);
  }
  
  public void setRegex(String pattern)
  { this.pattern=Pattern.compile(pattern);
  }
 

  @Override
  public Channel<Violation<T>> bindChannel(
    Focus<T> focus)
    throws BindException
  { return new PatternRuleChannel(focus);
  }

  /**
   * <p>Indicate that a null input will not trigger a violation
   * </p>
   * 
   * @param ignoreNull
   */
  public void setIgnoreNull(boolean ignoreNull)
  { this.ignoreNull=ignoreNull;
  }
  
  class PatternRuleChannel
    extends RuleChannel<T>
  {
    
    private final Channel<T> source;
    
    public PatternRuleChannel(Focus<T> focus)
    { source=focus.getSubject();
    }
    
    @Override
    protected Violation<T> retrieve()
    {
      T value=source.get();
      if (value==null && ignoreNull)
      { return null;
      }
      if (value==null || !pattern.matcher(value.toString()).matches())
      { return new Violation<T>(PatternRule.this,getMessage());
      }
      return null;
    }

  }
  
}

