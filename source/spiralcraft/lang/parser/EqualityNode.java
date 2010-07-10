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


import spiralcraft.lang.Channel;
//import spiralcraft.log.ClassLogger;
import spiralcraft.lang.Reflector;


public class EqualityNode<X>
  extends LogicalNode<X,X>
{
//  private static final ClassLogger log
//    =ClassLogger.getInstance(EqualityNode.class);

  private final boolean _negate;

  public EqualityNode(boolean negate,Node op1,Node op2)
  { 
    super(op1,op2);
    _negate=negate;
  }
  
  
  @Override
  public Node copy(Object visitor)
  { 
    EqualityNode<X> copy
      =new EqualityNode<X>(_negate,_op1.copy(visitor),_op2.copy(visitor));
    if (sameOperandNodes(copy))
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return reconstruct(_negate?"!=":"==");
  }

  @Override
  protected LogicalTranslator 
    newTranslator(Reflector<X> r1,Reflector<X> r2)
  { 
    return new LogicalTranslator()
    {  
      
      public Boolean translateForGet(X val,Channel<?>[] mods)
      { 
        Object mod=mods[0].get();
        //    log.fine("EqualityNode: "+val+" == "+mod);
        if (val==mod)
        { return _negate?Boolean.FALSE:Boolean.TRUE;
        }
        else if (val!=null && val.equals(mod))
        { return _negate?Boolean.FALSE:Boolean.TRUE;
        }
        return _negate?Boolean.TRUE:Boolean.FALSE;
      }
      
      /**
       * Equality is not a function because it calls "equals" on
       *   potentially mutable objects
       */
      public boolean isFunction()
      { return false;
      }
  
    };
  }
  
  public boolean isNegated()
  { return _negate;
  }
  
  @Override
  public String getSymbol()
  { return _negate?"!=":"==";
  }

}
