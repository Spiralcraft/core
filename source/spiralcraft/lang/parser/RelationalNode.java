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

public class RelationalNode<T1 extends Comparable<T1>,T2 extends T1>
  extends LogicalNode<T1,T2>
{

  private final boolean _greaterThan;
  private final boolean _equals;

  public RelationalNode(boolean greaterThan,boolean equals,Node op1,Node op2)
  { 
    super(op1,op2);
    _greaterThan=greaterThan;
    _equals=equals;
  }

  @SuppressWarnings("unchecked") // Array is heterogeneous
  public Boolean translateForGet(T1 val,Optic[] mods)
  { 
    Comparable<T1> val1= val;
    T2 val2=((Optic<T2>) mods[0]).get();
    
    if (val1==null)
    { 
      if (val2==null && _equals)
      { return true;
      }
      else
      { return null;
      }
    }
    
    int result=val1.compareTo(val2);
    
    if (_greaterThan)
    {
      if (_equals)
      { return result>=0;
      }
      else
      { return result>0;
      }
    }
    else
    {
      if (_equals)
      { return result<=0;
      }
      else
      { return result<0;
      }
    }
  }
  
  public T1 translateForSet(Boolean val,Optic[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { 
    if (_greaterThan)
    { 
      if (_equals)
      { return ">=";
      }
      else
      { return ">";
      }
    }
    else
    {
      if (_equals)
      { return "<=";
      }
      else
      { return "<";
      }
    }
  }

}
