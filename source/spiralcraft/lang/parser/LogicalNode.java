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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.LenseBinding;

public abstract class LogicalNode<T1,T2>
  extends BooleanNode
  implements Lense<Boolean,T1>
{
  public static Prism<Boolean> BOOLEAN_PRISM;
  
  private final Node _op1;
  private final Node _op2;

  public LogicalNode(Node op1,Node op2)
  { 
    _op1=op1;
    _op2=op2;
  }

  public Optic<Boolean> bind(Focus<?> focus)
    throws BindException
  { 
//    System.out.println("LogicalNode bind "+_op1.toString()+" "+_op2.toString());

    Optic[] params;
    if (_op2!=null)
    { params=new Optic[] {focus.bind(new Expression<T2>(_op2,null))};
    }
    else
    { params=new Optic[] {};
    }
    
    return new LenseBinding<Boolean,T1>
      (focus.bind(new Expression<T1>(_op1,null))
      ,this
      ,params
      );
      
  }
  
  public abstract String getSymbol();

  
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append(prefix).append(getClass().getName());
    prefix=prefix+"  ";
    if (_op2!=null)
    { 
      _op1.dumpTree(out,prefix);
      out.append(prefix).append(getSymbol());
      _op2.dumpTree(out,prefix);
    }
    else
    {
      out.append(prefix).append(getSymbol());
      _op1.dumpTree(out,prefix);
    }
  }
}
