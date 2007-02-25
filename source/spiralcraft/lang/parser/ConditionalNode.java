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
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Lense;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class ConditionalNode<T>
  extends Node<T>
{
  private final Node<Boolean> _condition;
  private final Node<T> _trueResult;
  private final Node<T> _falseResult;

  public ConditionalNode
    (Node<Boolean> condition
    ,Node<T> trueResult
    ,Node<T> falseResult
    )
  { 
    _condition=condition;
    _trueResult=trueResult;
    _falseResult=falseResult;
  }

  public Optic<T> bind(Focus<?> focus)
    throws BindException
  { 
    Optic<Boolean> condition=_condition.bind(focus);
    Optic<T> trueResult=_trueResult.bind(focus);
    Optic<T> falseResult=_falseResult.bind(focus);
    
    return new LenseBinding<T,Boolean>
      (condition
      ,new ConditionalLense<T>
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

class ConditionalLense<T>
  implements Lense<T,Boolean>
{
  private Prism<T> prism;
  
  public ConditionalLense(Prism<T> truePrism,Prism<T> falsePrism)
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
  
  public Prism<T> getPrism()
  { return prism;
  }
  
  @SuppressWarnings("unchecked") // Arrays and Generics issue
  public T translateForGet(Boolean val,Optic[] modifiers)
  { 
    if (val==null)
    { return ((Optic<T>) modifiers[1]).get();
    }
    return val?((Optic<T>)modifiers[0]).get():((Optic<T>)modifiers[1]).get();
  }

  public Boolean translateForSet(T val,Optic[] modifiers)
  { 
    // TODO: We can check which one of the modifiers "val" equals and
    //   set the boolean value accordingly
    
    throw new UnsupportedOperationException();
  }
}
