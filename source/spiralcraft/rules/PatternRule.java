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
  private boolean reject;
  
  public PatternRule()
  { }
  
  public PatternRule(String pattern)
  { setRegex(pattern);
  }
  
  public void setRegex(String pattern)
  { this.pattern=Pattern.compile(pattern);
  }
 

  public void setPattern(Pattern pattern)
  { this.pattern=pattern;
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
  
  /**
   * <p>Indicate that a matching value should be rejected.
   * </p>
   * 
   * @param reject
   */
  public void setRejectMatch(boolean reject)
  { this.reject=reject;
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
      if (!reject)
      {
        if (value==null || !pattern.matcher(value.toString()).matches())
        { return new Violation<T>(PatternRule.this,getMessage());
        }
      }
      else
      {
        if (value!=null && pattern.matcher(value.toString()).matches())
        { return new Violation<T>(PatternRule.this,getMessage());
        }
      }
      return null;
    }

  }
  
}

