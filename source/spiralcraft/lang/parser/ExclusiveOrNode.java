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
import spiralcraft.lang.Reflector;


public class ExclusiveOrNode
  extends LogicalNode<Boolean,Boolean>
{

  public ExclusiveOrNode(Node op1,Node op2)
  { super(op1,op2);
    
    // debugTree();
    // System.out.println("ExclusiveOrNode:"+op1.toString()+"\r\n"+op2.toString());
    
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    ExclusiveOrNode copy
      =new ExclusiveOrNode(_op1.copy(visitor),_op2.copy(visitor));
    if (sameOperandNodes(copy))
    { return this;
    }
    else
    { return copy;
    } 
  }
  
  @Override
  public String reconstruct()
  { return reconstruct("^^");
  }
  
  @Override
  protected LogicalTranslator 
    newTranslator(Reflector<Boolean> r1,Reflector<Boolean> r2)
  { 
    return new LogicalTranslator()
    {
      @Override
      @SuppressWarnings("unchecked") // Heterogeneous Array
      public Boolean translateForGet(Boolean val,Channel<?>[] mods)
      { 
        if (val==null)
        { return null;
        }
        boolean val1=val.booleanValue();
        //    System.out.println("val1="+val1);

        Boolean mod=((Channel<Boolean>) mods[0]).get();
        if (mod==null)
        { return null;
        }
        boolean val2=mod.booleanValue();
        //    System.out.println("val2="+val2);

        return (val1 || val2) && !(val1 && val2);


      }
      
      /**
       * XOR is a Function
       */
      @Override
      public boolean isFunction()
      { return true;
      }
    };
  }
    
  
  public Boolean translateForSet(Boolean val,Channel<?>[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  @Override
  public String getSymbol()
  { return "^";
  }
}
