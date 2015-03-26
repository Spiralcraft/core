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

/**
 * <p>Evaluates a condition, and returns the result of the positive case
 *   if the condition evalues to "true", otherwise returns the result of
 *   the negative case.
 * </p>
 * 
 * <p>A null value always returns the result of the negative case
 * </p>
 * 
 * @author mike
 */

@SuppressWarnings("unchecked") // Nodes are not generic
public class NullDefaultNode
  extends Node
{
  private final Node _normalResult;
  private final Node _nullResult;

  public NullDefaultNode
    (Node normalResult
    ,Node nullResult
    )
  { 
    _normalResult=normalResult;
    _nullResult=nullResult;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_normalResult,_nullResult};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    NullDefaultNode copy
      =new NullDefaultNode
      (_normalResult.copy(visitor)
      ,_nullResult.copy(visitor)
      );
    if (copy._normalResult==_normalResult
        && copy._nullResult==_nullResult
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
    return _normalResult.reconstruct()
      +" ?? "+_nullResult.reconstruct();
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Channel bind(Focus focus)
    throws BindException
  { 
    Channel normalResult=_normalResult.bind(focus);
    Channel nullResult=_nullResult.bind(focus);
    
    return new TranslatorChannel
      (normalResult
      ,new NullDefaultTranslator
        (normalResult.getReflector()
        ,nullResult.getReflector()
        )
      ,new Channel[] {nullResult}
      );
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Condition");
    prefix=prefix+"  ";
    _normalResult.dumpTree(out,prefix);
    out.append(prefix).append("??");
    _nullResult.dumpTree(out,prefix);
  }
}

class NullDefaultTranslator<T>
  implements Translator<T,T>
{
  private Reflector<T> reflector;
  
  @SuppressWarnings({"unchecked","rawtypes"}) // Type comparison
  public NullDefaultTranslator
    (Reflector normalReflector
    ,Reflector<T> nullReflector
    )
    throws BindException
  { 
    reflector=(Reflector<T>) normalReflector.getCommonType(nullReflector);
    if (reflector==null)
    { 
      throw new BindException
        ("Argument types "
        +normalReflector.getTypeURI()+" and "+nullReflector.getTypeURI()
        +" have nothing in common"
        );
    }    
    
  }
  
  @Override
  public Reflector<T> getReflector()
  { return reflector;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Arrays and Generics issue
  public T translateForGet(T val,Channel<?>[] modifiers)
  { 
    if (val!=null)
    { return val;
    }
    else
    { return (T) modifiers[0].get();
    }
  }

  @Override
  public T translateForSet(T val,Channel<?>[] modifiers)
  { return val;
  }
  
  /**
   * The conditional itself is a simple binary function
   */
  @Override
  public boolean isFunction()
  { return true;
  }
}
