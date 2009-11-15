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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;

public abstract class LogicalNode<T1,T2>
  extends BooleanNode
  implements Translator<Boolean,T1>
{
  protected final Node _op1;
  protected final Node _op2;

  public LogicalNode(Node op1,Node op2)
  { 
    _op1=op1;
    _op2=op2;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_op1,_op2};
  }
  
  @Override
  public Channel<Boolean> bind(Focus<?> focus)
    throws BindException
  { 
//    System.out.println("LogicalNode bind "+_op1.toString()+" "+_op2.toString());


    
    Channel<T1> op1Channel=focus.bind(new Expression<T1>(_op1,null));
    Expression<T2> op2Expr=_op2!=null?new Expression<T2>(_op2,null):null;
      
    // Give first operand a chance to implement the operator
    Channel<Boolean> resultChannel
      =op1Channel.getReflector().resolve
        (op1Channel
        ,focus
        ,getSymbol()
        , op2Expr!=null?new Expression[] {op2Expr}:null
        );
    
    if (resultChannel==null)
    {
      Channel<?>[] params;
      if (op2Expr!=null)
      { params=new Channel[] {focus.bind(op2Expr)};
      }
      else
      { params=new Channel[] {};
      }
      
      resultChannel
        =new TranslatorChannel<Boolean,T1>
          (op1Channel
          ,this
          ,params
          );
    }
    return resultChannel;
      
  }
  
  @Override
  public abstract String getSymbol();
  
  protected String reconstruct(String operator)
  { return _op1.reconstruct()+" "+operator+" "+_op2.reconstruct();
  }

  public Node getLeftOperand()
  { return _op1;
  }
  
  public Node getRightOperand()
  { return _op2;
  }
  
  @Override
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
