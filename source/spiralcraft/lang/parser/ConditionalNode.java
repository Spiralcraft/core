//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
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
  
  public Object translateForGet(Object val,Optic[] modifiers)
  { 
    if (val==null)
    { return modifiers[1].get();
    }
    return ((Boolean) val).booleanValue()?modifiers[0].get():modifiers[1].get();
  }

  public Object translateForSet(Object val,Optic[] modifiers)
  { throw new UnsupportedOperationException();
  }
}
