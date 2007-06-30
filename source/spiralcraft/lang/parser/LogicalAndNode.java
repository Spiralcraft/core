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


public class LogicalAndNode
  extends LogicalNode<Boolean,Boolean>
{

  public LogicalAndNode(Node op1,Node op2)
  { super(op1,op2);
  }

  @SuppressWarnings("unchecked") // Non-generic array
  public Boolean translateForGet(Boolean val,Channel[] mods)
  { 
    boolean val1=val!=null?val.booleanValue():false;
    if (!val1)
    { return Boolean.FALSE;
    }
    Boolean mod=((Channel<Boolean>)mods[0]).get();
    boolean val2=mod!=null?mod.booleanValue():false;
    return val2?Boolean.TRUE:Boolean.FALSE;
  }
  
  public Boolean translateForSet(Boolean val,Channel[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { return "&&";
  }

}
