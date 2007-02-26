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
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;


public class OpNode<T1,T2 extends T1>
  extends Node<T1>
{

  private final Node<T1> _op1;
  private final Node<T2> _op2;
  private final Expression[] _params;
  private final String _op;

  public OpNode(Node<T1> op1,Node<T2> op2,char op)
  { 
    _op1=op1;
    _op2=op2;
    _op=new String(new char[]{op}).intern();
    _params=new Expression[] {new Expression<T2>(_op2,null)};
  }

  public Optic<T1> bind(final Focus focus)
    throws BindException
  { 
    Optic<T1> op1=_op1.bind(focus);

    Optic<T1> ret=op1
      .resolve(focus
              ,_op
              ,_params
              );
    if (ret==null)
    { throw new BindException("Could not bind '"+_op+"' operator in "+op1.toString());
    }
    return ret;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Op "+_op);
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(_op);
    _op2.dumpTree(out,prefix);
  }

}
