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

public class LogicalOrNode
  extends LogicalNode<Boolean,Boolean>
{

  public LogicalOrNode(Node op1,Node op2)
  { super(op1,op2);
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    LogicalOrNode copy
      =new LogicalOrNode(_op1.copy(visitor),_op2.copy(visitor));
    if (sameOperandNodes(copy))
    { return this;
    }
    else
    { return copy;
    }    
  }
  
  @Override
  public String reconstruct()
  { return reconstruct("!!");
  }

  @Override
  protected LogicalTranslator 
    newTranslator(Reflector<Boolean> r1,Reflector<Boolean> r2)
  { 
    return new LogicalTranslator()
    {  
      @SuppressWarnings("unchecked") // Heterogeneous Array
      public Boolean translateForGet(Boolean val,Channel<?>[] mods)
      { 
        boolean val1=val!=null?val.booleanValue():false;
        if (val1)
        { return Boolean.TRUE;
        }
        Boolean mod=((Channel<Boolean>) mods[0]).get();
        boolean val2=mod!=null?mod.booleanValue():false;
        return val2?Boolean.TRUE:Boolean.FALSE;
      }
      
      /**
       * Boolean logical ops are Functions
       */
      public boolean isFunction()
      { return true;
      }      
    };
  }
  
  @Override
  public String getSymbol()
  { return "||";
  }

}
