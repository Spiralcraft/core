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

public class LogicalNegateNode
  extends LogicalNode<Boolean,Void>
{

  public LogicalNegateNode(Node node)
  { super(node,null);
  }

  @Override
  public Node copy(Object visitor)
  { 
    LogicalNegateNode copy
      =new LogicalNegateNode(_op1.copy(visitor));
    if (sameOperandNodes(copy))
    { return this;
    }
    else
    { return copy;
    }  
  }
  
  @Override
  public String reconstruct()
  { return " ! "+getLeftOperand().reconstruct();
  }
  
  @Override
  protected LogicalTranslator 
    newTranslator(Reflector<Boolean> r1,Reflector<Void> r2)
  { 
    return new LogicalTranslator()
    {  
      @Override
      public Boolean translateForGet(Boolean val,Channel<?>[] mods)
      { 
        // A "!" (not) operator returns true for null and false
        //
        // For a more explicit test, use "x==false"
        //
        if (val==null || val)
        { return Boolean.FALSE;
        }
        else
        { return Boolean.TRUE;
        }
      }

      @Override
      public Boolean translateForSet(Boolean val,Channel<?>[] mods)
      { 
        if (val==null)
        { return null;
        }
        else if (val)
        { return Boolean.FALSE;
        }
        else
        { return Boolean.TRUE;
        }
      }
      
      /**
       * Boolean logical ops are Functions
       */
      @Override
      public boolean isFunction()
      { return true;
      }      
    };
    
  }
  
  @Override
  public String getSymbol()
  { return "!";
  }
}
