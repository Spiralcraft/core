package spiralcraft.data.core;


import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.rules.AbstractRule;
import spiralcraft.rules.RuleChannel;
import spiralcraft.rules.Violation;


public class RequiredRule<T extends Tuple>
  extends AbstractRule<Type<T>,T>
{

  private Field<?> field;
  
  public RequiredRule(Type<T> type,Field<?> field)
  { 
    
    setContext(type);
    this.field=field;
    
    
  }    
  
  
  @Override
  public Channel<Violation<T>> bindChannel
    (Channel<T> source,Focus<?> focus,Expression<?>[] args)
    throws BindException
  { return new RequiredRuleChannel(source,focus);
  }
    
  class RequiredRuleChannel
    extends RuleChannel<T>
  {

    private final Channel<T> source;
    private final Channel<?> fieldChannel;

    
    @SuppressWarnings("unchecked") // Cast to tuple for field binding
    public RequiredRuleChannel(Channel<T> source,Focus<?> focus)
      throws BindException
    { 
      this.source=source;
      fieldChannel=field.bindChannel((Channel<Tuple>) source,focus,null);
    }      
          
    @Override
    protected Violation<T> retrieve()
    {
      if (source.get()==null)
      { return null;
      }
      
      if (fieldChannel.get()==null)
      {
        return new Violation<T>
          (RequiredRule.this,field.getTitle()+" must contain a value");
      }
      return null;      
    }
  }
 
}
