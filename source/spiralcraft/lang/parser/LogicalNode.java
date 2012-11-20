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


import spiralcraft.common.Coercion;
import spiralcraft.lang.Channel;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;
import spiralcraft.util.lang.NumericCoercion;

public abstract class LogicalNode<T1,T2>
  extends BooleanNode
{
  protected final Node _op1;
  protected final Node _op2;

  public LogicalNode(Node op1,Node op2)
  { 
    _op1=op1;
    _op2=op2;
    hashCode=computeHashCode();
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


    
    Channel<T1> op1Channel=focus.bind(Expression.<T1>create(_op1,null));
    Expression<T2> op2Expr=_op2!=null?Expression.<T2>create(_op2,null):null;
      
    // Give first operand a chance to implement the operator
    Channel<Boolean> resultChannel
      =op1Channel.getReflector().resolve
        (op1Channel
        ,focus
        ,getSymbol()
        , op2Expr!=null?new Expression<?>[] {op2Expr}:null
        );
    
    if (resultChannel==null)
    {
      Channel<T2> op2Channel=null;
      
      Channel<?>[] params;
      if (op2Expr!=null)
      { 
        op2Channel=focus.bind(op2Expr);
        params=new Channel<?>[] {op2Channel};
      }
      else
      { params=new Channel<?>[] {};
      }
      
      resultChannel
        =new TranslatorChannel<Boolean,T1>
          (op1Channel
          ,newTranslator
            (op1Channel.getReflector(),op2Channel!=null
                ?op2Channel.getReflector()
                :null
            )
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
  
  protected boolean sameOperandNodes(LogicalNode<T1,T2> copy)
  { return copy._op1==_op1 && copy._op2==_op2;
  }

  private int computeHashCode()
  { return ArrayUtil.arrayHashCode(new Object[] {_op1,_op2});
  }
  
  @Override
  protected boolean equalsNode(Node node)
  {
    LogicalNode<?,?> mynode=(LogicalNode<?,?>) node;
    return ClassUtil.equals(_op1,mynode._op1)
      && ClassUtil.equals(_op1,mynode._op2);
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
  
  protected abstract LogicalTranslator 
    newTranslator(Reflector<T1> r1,Reflector<T2> r2)
      throws BindException;
  
  abstract class LogicalTranslator
    implements Translator<Boolean,T1>
  {
    
    @Override
    public Reflector<Boolean> getReflector()
    { return BOOLEAN_REFLECTOR;
    }
    
    @Override
    public T1 translateForSet(Boolean val,Channel<?>[] mods)
    { 
      // Not reversible
      throw new UnsupportedOperationException();
    }    
  }
  
  abstract class RelationalTranslator
    extends LogicalTranslator
  {
    protected final Coercion<T2,T1> coercion;
    
    @SuppressWarnings("unchecked")
    protected RelationalTranslator(Reflector<T1> r1, Reflector<T2> r2)
      throws BindException
    {
      if (r1.getContentType()!=r2.getContentType())
      {
        if (Number.class.isAssignableFrom(r1.getContentType())
            && Number.class.isAssignableFrom(r2.getContentType()))
        { 
          coercion
            =(Coercion<T2,T1>) NumericCoercion.instance(r1.getContentType());
        }
        else
        { coercion=null;
        }
      } 
      else
      { coercion=null;
      }
      
      checkTypes();
    }
    
    protected void checkTypes()
      throws BindException
    {
    }
    
    
  }
}
