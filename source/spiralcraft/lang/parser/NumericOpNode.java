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
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.StringConcatLense;

import spiralcraft.util.ClassUtil;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;


public class NumericOpNode
  extends Node
{

  private static Lense _stringConcatLense;
  
  private static HashMap<Class,NumericLense> _lenseMapAdd
    =new HashMap<Class,NumericLense>();
  
  private static HashMap<Class,NumericLense> _lenseMapSubtract
    =new HashMap<Class,NumericLense>();

  private static HashMap<Class,NumericLense> _lenseMapMultiply
    =new HashMap<Class,NumericLense>();

  private static HashMap<Class,NumericLense> _lenseMapDivide
    =new HashMap<Class,NumericLense>();

  static
  {
    try
    { 
      _stringConcatLense
        =new StringConcatLense
          (OpticFactory.getInstance().findPrism(String.class)
          );
    }
    catch (BindException x)
    { x.printStackTrace();
    }
  }
    
  private final Node _op1;
  private final Node _op2;
  private final char _op;
  

    
  public NumericOpNode(Node op1,Node op2,char op)
  { 
    _op1=op1;
    _op2=op2;
    _op=op;
  }

  
  public Optic bind(Focus focus)
    throws BindException
  {
    
    Optic op1=focus.bind(new Expression(_op1,null));
    Optic op2=focus.bind(new Expression(_op2,null));
    
    if (String.class.isAssignableFrom(op1.getContentType()))
    { return bindString(focus,op1,op2);
    }
    else if (ClassUtil.isNumber(op1.getContentType()))
    { return bindNumber(focus,op1,op2);
    }
    else
    { 
      throw new BindException
        ("Can't apply '"+_op+"' operator to "
        +op1.getContentType().getName()
        );
    }
  }
  
  private Optic bindString(Focus focus,Optic op1,Optic op2)
    throws BindException
  {
    if (_op=='+')
    {
      return new LenseBinding
        (op1
        ,_stringConcatLense
        ,new Optic[] {op2}
        );
    }
    else
    {
      throw new BindException
        ("Can't apply '"+_op+"' operator to "
        +op1.getContentType().getName()
        );
    }
    
  }
  
  private Optic bindNumber(Focus focus,Optic op1,Optic op2)
    throws BindException
  {
    
    Prism prism=OpticFactory.getInstance().findPrism
      (promotedType(op1.getContentType(),op2.getContentType()));
    
    HashMap<Class,NumericLense> lenseMap=null;
    switch (_op)
    {
      case '+':
        lenseMap=_lenseMapAdd;
        break;
      case '-':
        lenseMap=_lenseMapSubtract;
        break;
      case '*':
        lenseMap=_lenseMapMultiply;
        break;
      case '/':
        lenseMap=_lenseMapDivide;
        break;
    }
    
    NumericLense lense=lenseMap.get(prism.getContentType());
    
    if (lense==null)
    { 
      Class clazz=prism.getContentType();
      
      if (clazz==Integer.class || clazz==int.class)
      {
        lense=new NumericLense(prism,_op)
        {
            
          public Object get(Object val1,Object val2)
          { 
            Number n1=(Number) val1;
            Number n2=(Number) val2;

            if (val1==null)
            { return val2;
            }
            if (val2==null)
            { return val1;
            }
            
            switch (oper)
            {
              case '+':
                return (int) (n1.intValue()+n2.intValue());
              case '-':
                return (int) (n1.intValue()-n2.intValue());
              case '*':
                return (int) (n1.intValue()*n2.intValue());
              case '/':
                return (int) (n1.intValue()/n2.intValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==Float.class || clazz==float.class)
      {
        lense=new NumericLense(prism,_op)
        {
            
          public Object get(Object val1,Object val2)
          { 
            Number n1=(Number) val1;
            Number n2=(Number) val2;

            if (val1==null)
            { return val2;
            }
            if (val2==null)
            { return val1;
            }
            
            switch (oper)
            {
              case '+':
                return (float) (n1.floatValue()+n2.floatValue());
              case '-':
                return (float) (n1.floatValue()-n2.floatValue());
              case '*':
                return (float) (n1.floatValue()*n2.floatValue());
              case '/':
                return (float) (n1.floatValue()/n2.floatValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==Double.class || clazz==double.class)
      {
        lense=new NumericLense(prism,_op)
        {
            
          public Object get(Object val1,Object val2)
          { 
            Number n1=(Number) val1;
            Number n2=(Number) val2;

            if (val1==null)
            { return val2;
            }
            if (val2==null)
            { return val1;
            }
            
            switch (oper)
            {
              case '+':
                return (double) (n1.doubleValue()+n2.doubleValue());
              case '-':
                return (double) (n1.doubleValue()-n2.doubleValue());
              case '*':
                return (double) (n1.doubleValue()*n2.doubleValue());
              case '/':
                return (double) (n1.doubleValue()/n2.doubleValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==BigDecimal.class)
      {
        lense=new NumericLense(prism,_op)
        {
            
          public Object get(Object val1,Object val2)
          { 

            if (val1==null)
            { return val2;
            }
            if (val2==null)
            { return val1;
            }

            BigDecimal num1;
            BigDecimal num2;
            if (val1 instanceof BigDecimal)
            { num1=(BigDecimal) val1;
            }
            else 
            { num1=new BigDecimal(val1.toString());
            }
            if (val2 instanceof BigDecimal)
            { num2=(BigDecimal) val2;
            }
            else 
            { num2=new BigDecimal(val2.toString());
            }

            
            switch (oper)
            {
              case '+':
                return num1.add(num2);
              case '-':
                return num1.subtract(num2);
              case '*':
                return num1.multiply(num2);
              case '/':
                return num1.divide(num2);
              default:
                return null;                
            }
          }
        };
      }
      else
      { throw new BindException("Don't know how to handle a "+clazz);
      }
      
      lenseMap.put(op1.getContentType(),lense);
    }
    
    return new LenseBinding
      (op1
      ,lense
      ,new Optic[] {op2}
      );
  }
  
  
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append(_op);
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(":");
    _op2.dumpTree(out,prefix);
  }

  private static Class promotedType(Class cl1,Class cl2)
    throws BindException
  {
    cl1=ClassUtil.boxedEquivalent(cl1);
    cl2=ClassUtil.boxedEquivalent(cl2);
    
    if (cl1==Integer.class)
    {
      if (cl2==Integer.class)
      { return Integer.class;
      }
      else if (cl2==Byte.class)
      { return Integer.class;
      }
      else if (cl2==Short.class)
      { return Integer.class;
      }
      else if (cl2==Float.class)
      { return Float.class;
      }
      else if (cl2==Long.class)
      { return Long.class;
      }
      else if (cl2==Double.class)
      { return Double.class;
      }
      else if (cl2==BigInteger.class)
      { return BigInteger.class;
      }
      else if (cl2==BigDecimal.class)
      { return BigDecimal.class;
      }
    }
    else if (cl1==Byte.class)
    {
    }
    else if (cl1==Short.class)
    {
    }
    else if (cl1==Float.class)
    {
      if (cl2==Integer.class)
      { return Float.class;
      }
      else if (cl2==Byte.class)
      { return Float.class;
      }
      else if (cl2==Short.class)
      { return Float.class;
      }
      else if (cl2==Float.class)
      { return Float.class;
      }
      else if (cl2==Long.class)
      { return Double.class;
      }
      else if (cl2==Double.class)
      { return Double.class;
      }
      else if (cl2==BigInteger.class)
      { return BigDecimal.class;
      }
      else if (cl2==BigDecimal.class)
      { return BigDecimal.class;
      }
    }
    else if (cl1==Long.class)
    {
      if (cl2==Integer.class)
      { return Long.class;
      }
      else if (cl2==Byte.class)
      { return Long.class;
      }
      else if (cl2==Short.class)
      { return Long.class;
      }
      else if (cl2==Float.class)
      { return Double.class;
      }
      else if (cl2==Long.class)
      { return Long.class;
      }
      else if (cl2==Double.class)
      { return Double.class;
      }
      else if (cl2==BigInteger.class)
      { return BigInteger.class;
      }
      else if (cl2==BigDecimal.class)
      { return BigDecimal.class;
      }
    }
    else if (cl1==Double.class)
    {
      if (cl2==Integer.class)
      { return Double.class;
      }
      else if (cl2==Byte.class)
      { return Double.class;
      }
      else if (cl2==Short.class)
      { return Double.class;
      }
      else if (cl2==Float.class)
      { return Double.class;
      }
      else if (cl2==Long.class)
      { return Double.class;
      }
      else if (cl2==Double.class)
      { return Double.class;
      }
      else if (cl2==BigInteger.class)
      { return BigDecimal.class;
      }
      else if (cl2==BigDecimal.class)
      { return BigDecimal.class;
      }
    }
    else if (cl1==BigInteger.class)
    { return BigInteger.class;
    }
    else if (cl1==BigDecimal.class)
    { return BigDecimal.class;
    }
    throw new BindException
      ("Incompatible types "
      +cl1.getName()
      +" and "
      +cl2.getName()
      );
  }
}

abstract class NumericLense
  implements Lense
{ 
  private Prism prism;
  protected char oper;
  
  public NumericLense(Prism prism,char op)
  { 
    this.prism=prism;
    this.oper=op;
  }
  
  protected abstract Object get(Object val1,Object val2);
  
  protected Object set(Object val1,Object val2)
  { throw new UnsupportedOperationException(oper+" is not reversible");
  }

  public Object translateForGet(Object val,Optic[] mods)
  { return get(val,mods[0].get());
  }
  
  public Object translateForSet(Object val,Optic[] mods)
  { return set(val,mods[0].get());
  }

  public Prism getPrism()
  { return prism;
  }
}