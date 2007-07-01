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
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorBinding;
import spiralcraft.lang.spi.StringConcatTranslator;

import spiralcraft.util.lang.ClassUtil;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;


public class NumericOpNode<T1 extends Comparable<T1>,T2>
  extends Node
{
 

    
  private final Node _op1;
  private final Node _op2;
  private final char _op;
  

    
  public NumericOpNode(Node op1,Node op2,char op)
  { 

    _op1=op1;
    _op2=op2;
    _op=op;
//    System.out.println("NumericOpNoe init: "+op+" : "+op1+" : "+op2);
  }

  
  @SuppressWarnings("unchecked") // More heterogeneus operations
  public Channel<T1> bind(Focus<?> focus)
    throws BindException
  {
    
    Channel<T1> op1=focus.<T1>bind(new Expression<T1>(_op1,null));
    Channel<T2> op2=focus.<T2>bind(new Expression<T2>(_op2,null));
    
    if (String.class.isAssignableFrom(op1.getContentType()))
    { return (Channel<T1>) StringBindingHelper.bindString(focus,(Channel<String>) op1,op2,_op);
    }
    else if (ClassUtil.isNumber(op1.getContentType()))
    { 
      return (Channel<T1>) NumberBindingHelper.bindNumber
        (focus
        ,(Channel<? extends Number>) op1
        ,(Channel<? extends Number>) op2
        ,_op
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


  
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append(_op);
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(":");
    _op2.dumpTree(out,prefix);
  }

}

class StringBindingHelper
{

  private static Translator<String,String> _stringConcatTranslator;
  static
  {
    try
    { 
      _stringConcatTranslator
        =new StringConcatTranslator
          (BeanReflector.<String>getInstance(String.class)
          );
    }
    catch (BindException x)
    { x.printStackTrace();
    }
  }
  
  public static final Channel<String> 
    bindString(Focus focus,Channel<String> op1,Channel<?> op2,char operator)
    throws BindException
  {
    if (operator=='+')
    {
      return new TranslatorBinding<String,String>
        (op1
        ,_stringConcatTranslator
        ,new Channel[] {op2}
        );
    }
    else
    {
      throw new BindException
        ("Can't apply '"+operator+"' operator to "
        +op1.getContentType().getName()
        );
    }
    
  }
}


class NumberBindingHelper
{
  //XXX There is a clash here between generics and dymamic selection of which type to
  //promote as the result in a numeric operation. The generic method holds to the source
  //type and not the argument type.
  
  private static HashMap<Class,NumericTranslator> _translatorMapAdd
    =new HashMap<Class,NumericTranslator>();
  
  private static HashMap<Class,NumericTranslator> _translatorMapSubtract
    =new HashMap<Class,NumericTranslator>();

  private static HashMap<Class,NumericTranslator> _translatorMapMultiply
    =new HashMap<Class,NumericTranslator>();

  private static HashMap<Class,NumericTranslator> _translatorMapDivide
    =new HashMap<Class,NumericTranslator>();
  
  @SuppressWarnings("unchecked")
  public static final <Tret extends Number,T1 extends Tret,T2 extends Tret> Channel<Tret> 
    bindNumber(Focus<?> focus,Channel<T1> op1,Channel<T2> op2,char operator)
    throws BindException
  {
    
    // Promoted type might not be T1, but not a runtime problem.
    
    Reflector<Tret> reflector=BeanReflector.<Tret>getInstance
      (promotedType(op1.getContentType(),op2.getContentType()));
    
    HashMap<Class,NumericTranslator> translatorMap=null;
    switch (operator)
    {
      case '+':
        translatorMap=_translatorMapAdd;
        break;
      case '-':
        translatorMap=_translatorMapSubtract;
        break;
      case '*':
        translatorMap=_translatorMapMultiply;
        break;
      case '/':
        translatorMap=_translatorMapDivide;
        break;
    }
    
    NumericTranslator<Tret,T1,T2> translator=translatorMap.get(reflector.getContentType());
    
    if (translator==null)
    { 
      Class clazz=reflector.getContentType();
      
      if (clazz==Integer.class || clazz==int.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
            
          public Tret get(T1 val1,T2 val2)
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
                return (Tret) Integer.valueOf(n1.intValue()+n2.intValue());
              case '-':
                return (Tret) Integer.valueOf(n1.intValue()-n2.intValue());
              case '*':
                return (Tret) Integer.valueOf(n1.intValue()*n2.intValue());
              case '/':
                return (Tret) Integer.valueOf(n1.intValue()/n2.intValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==Float.class || clazz==float.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
            
          public Tret get(T1 val1,T2 val2)
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
                return (Tret) Float.valueOf(n1.floatValue()+n2.floatValue());
              case '-':
                return (Tret) Float.valueOf(n1.floatValue()-n2.floatValue());
              case '*':
                return (Tret) Float.valueOf(n1.floatValue()*n2.floatValue());
              case '/':
                return (Tret) Float.valueOf(n1.floatValue()/n2.floatValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==Double.class || clazz==double.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
            
          public Tret get(T1 val1,T2 val2)
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
                return (Tret) Double.valueOf(n1.doubleValue()+n2.doubleValue());
              case '-':
                return (Tret) Double.valueOf(n1.doubleValue()-n2.doubleValue());
              case '*':
                return (Tret) Double.valueOf(n1.doubleValue()*n2.doubleValue());
              case '/':
                return (Tret) Double.valueOf(n1.doubleValue()/n2.doubleValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==BigDecimal.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
            
          public Tret get(T1 val1,T2 val2)
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
                return (Tret) num1.add(num2);
              case '-':
                return (Tret) num1.subtract(num2);
              case '*':
                return (Tret) num1.multiply(num2);
              case '/':
                return (Tret) num1.divide(num2);
              default:
                return null;                
            }
          }
        };
      }
      else
      { throw new BindException("Don't know how to handle a "+clazz);
      }
      
      translatorMap.put(op1.getContentType(),translator);
    }
    
    return new TranslatorBinding<Tret,T1>
      (op1
      ,translator
      ,new Channel[] {op2}
      );
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

abstract class NumericTranslator<Tret extends Number,T1 extends Tret,T2 extends Tret>
  implements Translator<Tret,T1>
{ 
  private Reflector<Tret> reflector;
  protected char oper;
  
  public NumericTranslator(Reflector<Tret> reflector,char op)
  { 
    this.reflector=reflector;
    this.oper=op;
  }
  
  protected abstract Tret get(T1 val1,T2 val2);
  
  protected T1 set(Tret val1,T2 val2)
  { throw new UnsupportedOperationException(oper+" is not reversible");
  }

  @SuppressWarnings("unchecked") // Heterogeneous Array
  public Tret translateForGet(T1 val,Channel[] mods)
  { return get(val,((Channel<T2>) mods[0]).get());
  }
  
  @SuppressWarnings("unchecked") // Heterogeneous Array
  public T1 translateForSet(Tret val,Channel[] mods)
  { return set(val,((Channel<T2>) mods[0]).get());
  }

  public Reflector<Tret> getReflector()
  { return reflector;
  }
}