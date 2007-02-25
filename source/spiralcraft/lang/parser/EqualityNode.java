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

import spiralcraft.lang.Optic;

public class EqualityNode<X>
  extends LogicalNode<X,X>
{

  private final boolean _negate;

  public EqualityNode(boolean negate,Node<X> op1,Node<X> op2)
  { 
    super(op1,op2);
    _negate=negate;
  }

  public Boolean translateForGet(X val,Optic[] mods)
  { 
    Object mod=mods[0].get();
    if (val==mod)
    { return _negate?Boolean.FALSE:Boolean.TRUE;
    }
    else if (val!=null && val.equals(mod))
    { return _negate?Boolean.FALSE:Boolean.TRUE;
    }
    return _negate?Boolean.TRUE:Boolean.FALSE;
  }
  
  public X translateForSet(Boolean val,Optic[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { return _negate?"!=":"==";
  }

}
