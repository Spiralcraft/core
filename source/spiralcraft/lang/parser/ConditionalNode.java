package spiralcraft.lang.parser;

import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.Binding;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Lense;

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
    
    return new LenseBinding
      (condition
      ,new ConditionalLense
        (trueResult.getPrism()
        ,falseResult.getPrism()
        )
      ,new Optic[] {trueResult,falseResult}
      );
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

class ConditionalLense
  implements Lense
{
  private Prism prism;
  
  public ConditionalLense(Prism truePrism,Prism falsePrism)
    throws BindException
  { 
    if (truePrism.getContentType()==Void.class)
    { prism=falsePrism;
    }
    else if (falsePrism.getContentType()==Void.class)
    { prism=truePrism;
    }
    else if (truePrism.getContentType().isAssignableFrom(falsePrism.getContentType()))
    { prism=truePrism;
    }
    else if (falsePrism.getContentType().isAssignableFrom(truePrism.getContentType()))
    { prism=falsePrism;
    }
    else
    { throw new BindException("Can't disambiguate conditional");
    }
  }
  
  public Prism getPrism()
  { return prism;
  }
  
  public Object translateForGet(Object val,Object[] modifiers)
  { 
    if (val==null)
    { return modifiers[1];
    }
    return ((Boolean) val).booleanValue()?modifiers[0]:modifiers[1];
  }

  public Object translateForSet(Object val,Object[] modifiers)
  { throw new UnsupportedOperationException();
  }
}
