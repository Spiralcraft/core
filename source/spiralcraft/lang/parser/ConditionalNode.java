package spiralcraft.lang.parser;

import spiralcraft.lang.optics.ConditionalBinding;
import spiralcraft.lang.optics.Binding;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class ConditionalNode
  extends Node
{
  private final Node _condition;
  private final Node _trueResult;
  private final Node _falseResult;
  

  public ConditionalNode
    (Node condition
    ,Node trueResult
    ,Node falseResult
    )
  { 
    _condition=condition;
    _trueResult=trueResult;
    _falseResult=falseResult;
  }

  public Optic bind(Focus focus)
    throws BindException
  { 
    Optic condition=_condition.bind(focus);
    Optic trueResult=_trueResult.bind(focus);
    Optic falseResult=_falseResult.bind(focus);
    
    return new ConditionalBinding
      (condition,trueResult,falseResult);
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Condition");
    prefix=prefix+"  ";
    _condition.dumpTree(out,prefix);
    out.append(prefix).append("?");
    _trueResult.dumpTree(out,prefix);
    out.append(prefix).append(":");
    _falseResult.dumpTree(out,prefix);
  }

}
