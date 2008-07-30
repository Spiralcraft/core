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


public class ExclusiveOrNode
  extends LogicalNode<Boolean,Boolean>
{

  public ExclusiveOrNode(Node op1,Node op2)
  { super(op1,op2);
    
    // debugTree();
    // System.out.println("ExclusiveOrNode:"+op1.toString()+"\r\n"+op2.toString());
    
  }
  
  public String reconstruct()
  { return reconstruct("^^");
  }

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
  
  public Boolean translateForSet(Boolean val,Channel<?>[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { return "^";
  }
}
