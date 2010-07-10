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

import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;

@SuppressWarnings("unchecked") // Nodes are not generic
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

  @Override
  public Node[] getSources()
  { return new Node[] {_condition,_trueResult,_falseResult};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    ConditionalNode copy
      =new ConditionalNode
      (_condition.copy(visitor)
      ,_trueResult.copy(visitor)
      ,_falseResult.copy(visitor)
      );
    if (copy._condition==_condition
        && copy._trueResult==_trueResult
        && copy._falseResult==_falseResult
       )
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { 
    return _condition.reconstruct()
      +" ? "+_trueResult.reconstruct()
      +" : "+_falseResult.reconstruct();
  }
  
  @Override
  public Channel bind(Focus focus)
    throws BindException
  { 
    Channel condition=_condition.bind(focus);
    Channel trueResult=_trueResult.bind(focus);
    Channel falseResult=_falseResult.bind(focus);
    
    return new TranslatorChannel
      (condition
      ,new ConditionalTranslator
        (trueResult.getReflector()
        ,falseResult.getReflector()
        )
      ,new Channel[] {trueResult,falseResult}
      );
  }
  
  @Override
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

class ConditionalTranslator<T>
  implements Translator<T,Boolean>
{
  private Reflector<T> reflector;
  
  @SuppressWarnings("unchecked") // Type comparison
  public ConditionalTranslator
    (Reflector<T> trueReflector
    ,Reflector<T> falseReflector
    )
    throws BindException
  { 
    reflector=(Reflector<T>) trueReflector.getCommonType(falseReflector);
    if (reflector==null)
    { 
      throw new BindException
        ("Argument types "
        +trueReflector.getTypeURI()+" and "+falseReflector.getTypeURI()
        +" have nothing in common"
        );
    }
    
  }
  
  public Reflector<T> getReflector()
  { return reflector;
  }
  
  @SuppressWarnings("unchecked") // Arrays and Generics issue
  public T translateForGet(Boolean val,Channel<?>[] modifiers)
  { 
    if (val==null)
    { return ((Channel<T>) modifiers[1]).get();
    }
    return val?((Channel<T>)modifiers[0]).get():((Channel<T>)modifiers[1]).get();
  }

  public Boolean translateForSet(T val,Channel<?>[] modifiers)
  { 
    // TODO: We can check which one of the modifiers "val" equals and
    //   set the boolean value accordingly
    
    throw new UnsupportedOperationException();
  }
  
  /**
   * The conditional itself is a simple binary function
   */
  public boolean isFunction()
  { return true;
  }
}
