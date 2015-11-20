package spiralcraft.task;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.rules.Inspector;
import spiralcraft.rules.Rule;
import spiralcraft.rules.RuleException;
import spiralcraft.rules.RuleSet;
import spiralcraft.rules.Violation;

/**
 * Evaluates an expression and returns the result
 * 
 * @author mike
 *
 * @param <Tcontext>
 * @param <Tresult>
 */
public class Validate<Tcontext,Tresult>
  extends Chain<Tcontext,Tresult>
{

  { 
    storeResults=true;
    addChainResult=true;
    importContext=true;
  }

  private Channel<Tcontext> contextChannel;
  private Binding<Void> onFailure;
  private RuleSet<Void,Tcontext> ruleSet;
  private Inspector<Void,Tcontext> inspector;

  
  public Validate()
  {
  }
  
  public void setRules(Rule<Void,Tcontext>[] rules)
  { 
    this.ruleSet=new RuleSet<Void,Tcontext>(null);
    this.ruleSet.addRules(rules);
  }
  
  public void setOnFailure(Binding<Void> onFailure)
  { this.onFailure=onFailure;
  }
  
  @Override
  protected Task task()
  { return new ValidateTask();
  }

  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void bindInContext(Focus<?> focus)
    throws BindException
  { 
    contextChannel=(Channel<Tcontext>) focus.getSubject();
    if (ruleSet!=null)
    {
      inspector
        =ruleSet.bind(contextChannel.getReflector(),focus);
    }
    if (onFailure!=null)
    { onFailure.bind(focus);
    }
    
    super.bindInContext(focus);
  }

 
  class ValidateTask 
    extends ChainTask
  {
    
    @Override
    public void work()
      throws InterruptedException
    {
       Violation<Tcontext>[] v=inspector.inspect(contextChannel.get());
      
       if (v!=null && v.length>0)
       { 
         addException(new RuleException(v));
         if (onFailure!=null)
         { onFailure.get();
         }
       }
       else
       { super.work();
       }
      
    }
      
  }
    
}