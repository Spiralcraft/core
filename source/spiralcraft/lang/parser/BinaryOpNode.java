//
// Copyright (c) 1998,2010 Michael Toth
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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.IterationDecorator;

import spiralcraft.lang.Reflector;

import spiralcraft.lang.functions.ToString;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.reflect.IterableReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.lang.spi.StringConcatTranslator;

import spiralcraft.util.lang.ClassUtil;
import spiralcraft.util.string.ArrayToString;
import spiralcraft.util.string.StringConverter;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.IterableChain;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p>An infix operator which has two arguments.
 * </p>
 * 
 * @author mike
 *
 * @param <T1>
 * @param <T2>
 */
public class BinaryOpNode<T1 extends Comparable<T1>,T2>
  extends Node
{
 

    
  private final Node _op1;
  private final Node _op2;
  private final char _op;
  

    
  public BinaryOpNode(Node op1,Node op2,char op)
  { 

    _op1=op1;
    _op2=op2;
    _op=op;
    hashCode=computeHashCode();
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_op1,_op2};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    BinaryOpNode<T1,T2> copy
      =new BinaryOpNode<T1,T2>(_op1.copy(visitor),_op2.copy(visitor),_op);
    if (copy._op1==_op1 && copy._op2==_op2)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return _op1.reconstruct()+_op+_op2.reconstruct();
  }
  
  @Override
  @SuppressWarnings({ "unchecked", "cast" }) // More heterogeneus operations
  public Channel<T1> bind(Focus<?> focus)
    throws BindException
  {
    
    Channel<T1> op1=focus.<T1>bind(Expression.<T1>create(_op1));
    Channel<T2> op2=focus.<T2>bind(Expression.<T2>create(_op2));
    
    if (String.class.isAssignableFrom(op1.getContentType()))
    { 
      return (Channel<T1>) (Object) 
        StringBindingHelper.bindString
          ((Channel<String>) (Object) op1,op2,_op);
    }
    else if (ClassUtil.isNumber(op1.getContentType()))
    { 
      Channel<T1> channel=(Channel<T1>) (Object)
        NumberBindingHelper.bindNumber
        ((Channel<? extends Number>) op1
        ,(Channel<? extends Number>) op2
        ,_op
        );
      if (channel==null)
      { 
        throw new BindException
          ("Incompatible types "
          +op1.getContentType().getName()
          +" and "
          +op2.getContentType().getName()
          +": "+reconstruct()
          );
      
      }
      channel.setContext(focus);
      return channel;
    }
    else if (op1.decorate(IterationDecorator.class)!=null)
    { 
      Channel<T1> channel=(Channel<T1>) IterationBindingHelper.bind(op1,op2,_op);
      channel.setContext(focus);
      return channel;
      
    }
    else
    { 
      throw new BindException
        ("Can't apply '"+_op+"' operator to "
        +op1.getContentType().getName()
        );
    }
  }


  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append(_op);
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(":");
    _op2.dumpTree(out,prefix);
  }

  
  private int computeHashCode()
  { return ArrayUtil.arrayHashCode(new Object[] {_op1,_op2,_op});
  }
  
  @Override
  protected boolean equalsNode(Node node)
  { 
    BinaryOpNode<?,?> mynode=(BinaryOpNode<?,?>) node;
    return ClassUtil.equals(_op1,mynode._op1)
        && ClassUtil.equals(_op2,mynode._op2)
        && ClassUtil.equals(_op,mynode._op);
  }

}


@SuppressWarnings({"unchecked","rawtypes"})
class IterationBindingHelper
{
  public static final Channel bind(Channel<?> t1,Channel<?> t2,char op)
    throws BindException
  {
    
    CollectionDecorator cd
      =t1.<CollectionDecorator>decorate(CollectionDecorator.class);
    Reflector collectionReflector=t1.getReflector();

    if (cd==null)
    { 
      cd=t2.<CollectionDecorator>decorate(CollectionDecorator.class);
      collectionReflector=t2.getReflector();
    }
    
    IterationDecorator d1
      =t1.<IterationDecorator>decorate(IterationDecorator.class);
    
    IterationDecorator d2
      =t2.<IterationDecorator>decorate(IterationDecorator.class);
    
    if (d2==null)
    {
      throw new BindException
        ("Second operand of '+' (concatenation) must be iterable. "
        +t2.getReflector()
        );
      
    }

    Reflector commonReflector
      =d1.getComponentReflector().getCommonType(d2.getComponentReflector());
        
    if (commonReflector==null)
    { 
      throw new BindException
        ("Component types of operands for '+' (concatenation) must have"
        +" a common supertype: "+d1.getComponentReflector()+" + "
        +d2.getComponentReflector()
        );
    }

    if (cd!=null && commonReflector==cd.getComponentReflector())
    { 
      return new CollectionConcatenationChannel
        (collectionReflector
        ,cd
        ,d1
        ,d2
        );
      
    }

    return new IterableConcatenationChannel(commonReflector,d1,d2);
  }
}

class CollectionConcatenationChannel<C,T>
  extends AbstractChannel<C>
{
  
  private final CollectionDecorator<C,T> cd;
  private final IterationDecorator<?,T> decorator1;
  private final IterationDecorator<?,T> decorator2;
  
  public CollectionConcatenationChannel
    (Reflector<C> collectionReflector
    ,CollectionDecorator<C,T> cd
    ,IterationDecorator<?,T> d1
    ,IterationDecorator<?,T> d2
    )
  { 
    super(collectionReflector);
    this.cd=cd;
    this.decorator1=d1;
    this.decorator2=d2;
    
    
  }

  @Override
  protected C retrieve()
  {
    C collection=cd.newCollection();
      
    collection=cd.addAll(collection,decorator1.iterator());
    collection=cd.addAll(collection,decorator2.iterator());
//    for (T val:decorator1)
//    { collection=cd.add(collection,val);
//    }
//    for (T val:decorator2)
//    { collection=cd.add(collection,val);
//    }
    return collection;
  }

  @Override
  protected boolean store(
    C val)
    throws AccessException
  { return false;
  }
  
}

class IterableConcatenationChannel<T>
  extends AbstractChannel<Iterable<T>>
{

  private final IterationDecorator<?,T> decorator1;
  private final IterationDecorator<?,T> decorator2;

  public IterableConcatenationChannel
    (Reflector<T> commonReflector
    ,IterationDecorator<?,T> d1
    ,IterationDecorator<?,T> d2
    )
  { 
    super(IterableReflector.getInstance(commonReflector));
    this.decorator1=d1;
    this.decorator2=d2;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Iterable<T> retrieve()
  { return new IterableChain<T>(decorator1,decorator2);
  }

  @Override
  protected boolean store(
    Iterable<T> val)
  throws AccessException
  { return false;
  }
}


class StringBindingHelper
{

  private static Translator<String,String> _stringConcatTranslator
    =new StringConcatTranslator
      (BeanReflector.<String>getInstance(String.class)
      );
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public  static final <S> Channel<String> 
    bindString(Channel<String> op1,Channel<S> op2,char operator)
    throws BindException
  {
    if (operator=='+')
    {
      Channel<String> stringOp2;
      if (op2.getReflector().getContentType()==String.class)
      { stringOp2=(Channel<String>) op2;
      }
      else
      { 
        StringConverter<S> converter=op2.getReflector().getStringConverter();
        if (converter==null)
        { 
          converter
            =StringConverter.getInstance(op2.getReflector().getContentType());
        }
        if (converter==null && op2.getReflector().getContentType().isArray())
        { 
          converter
            =new ArrayToString(op2.getReflector().getContentType().getComponentType());
        }
        if (converter==null)
        { converter=(StringConverter<S>) StringConverter.getOneWayInstance();
        }
        stringOp2=new ToString<S>(converter).bindChannel(op2,null,null);
      }
      
      return new TranslatorChannel<String,String>
        (op1
        ,_stringConcatTranslator
        ,new Channel<?>[] {stringOp2}
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
  
  private static HashMap<Class<?>,NumericTranslator<?,?,?>> _translatorMapAdd
    =new HashMap<Class<?>,NumericTranslator<?,?,?>>();
  
  private static HashMap<Class<?>,NumericTranslator<?,?,?>> _translatorMapSubtract
    =new HashMap<Class<?>,NumericTranslator<?,?,?>>();

  private static HashMap<Class<?>,NumericTranslator<?,?,?>> _translatorMapMultiply
    =new HashMap<Class<?>,NumericTranslator<?,?,?>>();

  private static HashMap<Class<?>,NumericTranslator<?,?,?>> _translatorMapDivide
    =new HashMap<Class<?>,NumericTranslator<?,?,?>>();
  
  private static HashMap<Class<?>,NumericTranslator<?,?,?>> _translatorMapModulus
    =new HashMap<Class<?>,NumericTranslator<?,?,?>>();
  
  private static StringConverter<BigDecimal> bigDecimalConverter
    = StringConverter.getInstance(BigDecimal.class);

  private static StringConverter<BigInteger> bigIntegerConverter
    = StringConverter.getInstance(BigInteger.class);
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final <Tret extends Number,T1 extends Tret,T2 extends Tret> Channel<Tret> 
    bindNumber(Channel<T1> op1,Channel<T2> op2,char operator)
    throws BindException
  {
    
    // Promoted type might not be T1, but not a runtime problem.
    
    Class promotedType=promotedType(op1.getContentType(),op2.getContentType());
    if (promotedType==null)
    { return null;
    }

    Reflector<Tret> reflector=BeanReflector.<Tret>getInstance(promotedType);
    
    HashMap<Class<?>,NumericTranslator<?,?,?>> translatorMap=null;
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
      case '%':
        translatorMap=_translatorMapModulus;
        break;
    }
    
    NumericTranslator<Tret,T1,T2> translator
      =(NumericTranslator<Tret,T1,T2>) 
        translatorMap.get(reflector.getContentType());
    
    if (translator==null)
    { 
      Class clazz=reflector.getContentType();
      
      if (clazz==Integer.class || clazz==int.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
            
          private Tret box(int prim)
          { return (Tret) Integer.valueOf(prim);
          }
          
          @Override
          public Tret get(T1 val1,T2 val2)
          { 
            Number n1= val1;
            Number n2= val2;

            if (val1==null)
            { return val2!=null?box(val2.intValue()):null;
            }
            if (val2==null)
            { return box(val1.intValue());
            }
            
            switch (oper)
            {
              case '+':
                return box(n1.intValue()+n2.intValue());
              case '-':
                return box(n1.intValue()-n2.intValue());
              case '*':
                return box(n1.intValue()*n2.intValue());
              case '/':
                return box(n1.intValue()/n2.intValue());
              case '%':
                return box(n1.intValue()%n2.intValue());
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
          private Tret box(float prim)
          { return (Tret) Float.valueOf(prim);
          }
            
          @Override
          public Tret get(T1 val1,T2 val2)
          { 
            Number n1=val1;
            Number n2=val2;

            if (val1==null)
            { return val2!=null?box(val2.floatValue()):null;
            }
            if (val2==null)
            { return box(val1.floatValue());
            }
            
            switch (oper)
            {
              case '+':
                return box(n1.floatValue()+n2.floatValue());
              case '-':
                return box(n1.floatValue()-n2.floatValue());
              case '*':
                return box(n1.floatValue()*n2.floatValue());
              case '/':
                return box(n1.floatValue()/n2.floatValue());
              case '%':
                return box(n1.floatValue()%n2.floatValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==Long.class || clazz==long.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
          private Tret box(long prim)
          { return (Tret) Long.valueOf(prim);
          }
            
          @Override
          public Tret get(T1 val1,T2 val2)
          { 
            Number n1= val1;
            Number n2= val2;

            if (val1==null)
            { return val2!=null?box(val2.longValue()):null;
            }
            if (val2==null)
            { return box(val1.longValue());
            }
            
            switch (oper)
            {
              case '+':
                return box(n1.longValue()+n2.longValue());
              case '-':
                return box(n1.longValue()-n2.longValue());
              case '*':
                return box(n1.longValue()*n2.longValue());
              case '/':
                return box(n1.longValue()/n2.longValue());
              case '%':
                return box(n1.longValue()%n2.longValue());
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
            
          private Tret box(double prim)
          { return (Tret) Double.valueOf(prim);
          }
          
          @Override
          public Tret get(T1 val1,T2 val2)
          { 
            Number n1= val1;
            Number n2= val2;

            if (val1==null)
            { return val2!=null?box(val2.doubleValue()):null;
            }
            if (val2==null)
            { return box(val1.doubleValue());
            }
            
            switch (oper)
            {
              case '+':
                return box(n1.doubleValue()+n2.doubleValue());
              case '-':
                return box(n1.doubleValue()-n2.doubleValue());
              case '*':
                return box(n1.doubleValue()*n2.doubleValue());
              case '/':
                return box(n1.doubleValue()/n2.doubleValue());
              case '%':
                return box(n1.doubleValue()%n2.doubleValue());
              default:
                return null;                
            }
          }
        };
      }
      else if (clazz==BigInteger.class)
      {
        translator=new NumericTranslator<Tret,T1,T2>(reflector,operator)
        {
            
          private Tret box(Number prim)
          { 
            if (prim instanceof BigInteger)
            { return (Tret) prim;
            }
            else
            { return (Tret) bigIntegerConverter.fromString(prim.toString());
            }
          }
          
          @Override
          public Tret get(T1 val1,T2 val2)
          { 

            if (val1==null)
            { return val2!=null?box(val2):null;
            }
            if (val2==null)
            { return box(val1);
            }

            BigInteger num1;
            BigInteger num2;
            if (val1 instanceof BigInteger)
            { num1=(BigInteger) val1;
            }
            else 
            { num1=bigIntegerConverter.fromString(val1.toString());
            }
            if (val2 instanceof BigInteger)
            { num2=(BigInteger) val2;
            }
            else 
            { num2=bigIntegerConverter.fromString(val2.toString());
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
              case '%':
                return (Tret) num1.remainder(num2);
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
            
          private Tret box(Number prim)
          { 
            if (prim instanceof BigDecimal)
            { return (Tret) prim;
            }
            else
            { return (Tret) bigDecimalConverter.fromString(prim.toString());
            }
          }
          
          @Override
          public Tret get(T1 val1,T2 val2)
          { 

            if (val1==null)
            { return val2!=null?box(val2):null;
            }
            if (val2==null)
            { return box(val1);
            }

            BigDecimal num1;
            BigDecimal num2;
            if (val1 instanceof BigDecimal)
            { num1=(BigDecimal) val1;
            }
            else 
            { num1=bigDecimalConverter.fromString(val1.toString());
            }
            if (val2 instanceof BigDecimal)
            { num2=(BigDecimal) val2;
            }
            else 
            { num2=bigDecimalConverter.fromString(val2.toString());
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
              case '%':
                return (Tret) num1.remainder(num2);
              default:
                return null;                
            }
          }
        };
      }
      else
      { throw new BindException("Don't know how to handle a "+clazz);
      }
      
      translatorMap.put(clazz,translator);
    }
    
    return new TranslatorChannel<Tret,T1>
      (op1
      ,translator
      ,new Channel[] {op2}
      );
  }
  
  

  private static Class<?> promotedType(Class<?> cl1,Class<?> cl2)
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
    return null;
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
  
  
  /**
   * 
   * @param val1
   * @param val2 
   */
  protected T1 set(Tret val1,T2 val2)
  { throw new UnsupportedOperationException(oper+" is not reversible");
  }

  @Override
  @SuppressWarnings("unchecked") // Heterogeneous Array
  public Tret translateForGet(T1 val,Channel<?>[] mods)
  { return get(val,((Channel<T2>) mods[0]).get());
  }
  
  @Override
  @SuppressWarnings("unchecked") // Heterogeneous Array
  public T1 translateForSet(Tret val,Channel<?>[] mods)
  { return set(val,((Channel<T2>) mods[0]).get());
  }

  /**
   * Arithmetic ops are functions in the formal sense
   */
  @Override
  public boolean isFunction()
  { return true;
  }

  @Override
  public Reflector<Tret> getReflector()
  { return reflector;
  }
  
  
}