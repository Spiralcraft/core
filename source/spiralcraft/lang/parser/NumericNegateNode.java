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

import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;

import spiralcraft.util.lang.ClassUtil;

public class NumericNegateNode<T extends Number>
  extends Node
{

  private final Node _node;

  public static HashMap<Class<?>,NegateTranslator<?>> _translatorMap
    =new HashMap<Class<?>,NegateTranslator<?>>();
  
  
  public NumericNegateNode(Node node)
  {  _node=node;
  }

    @Override
  public Node[] getSources()
  { return new Node[] {_node};
  }

  @Override
  public Node copy(Object visitor)
  { 
    NumericNegateNode<T> copy
      =new NumericNegateNode<T>(_node.copy(visitor));
    if (copy._node==_node)
    { return this;
    }
    else
    { return copy;
    }
  }

  @Override
  public String reconstruct()
  { return "-"+_node.reconstruct();
  }
  
  // Suppress Warnings notes:
  //   Translator cast at end of method from specific back to T- type checked against
  //   content type in type selector. Also Reflector is cast up to specific type, also safe
  //   due to API. There might be a cleaner way to do this. This class is heterogeneous.
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Channel<T> bind(Focus<?> focus)
    throws BindException
  {
    Channel<T> sourceBinding=focus.<T>bind(Expression.<T>create(_node));
    if (!Number.class.isAssignableFrom(ClassUtil.boxedEquivalent(sourceBinding.getContentType())))
    { throw new BindException("Negation operator only applies to numbers, not "+sourceBinding.getContentType());
    }

    NegateTranslator<? extends Number> translator=(NegateTranslator<T>) _translatorMap.get(sourceBinding.getContentType());
    Reflector<T> reflector=sourceBinding.getReflector();
    
    if (translator==null)
    { 
      Class clazz=ClassUtil.boxedEquivalent(sourceBinding.getContentType());
      if (clazz==Integer.class)
      {
        
        translator=new NegateTranslator<Integer>((Reflector<Integer>) reflector)
        {
          @Override
          public Integer negate(Integer val)
          { return -val;
          }
        };
      }
      else if (clazz==Float.class)
      {
        translator=new NegateTranslator<Float>((Reflector<Float>) reflector)
        {
          @Override
          public Float negate(Float val)
          { return -val;
          }
        };
      }
      else if (clazz==Byte.class)
      {
        translator=new NegateTranslator<Byte>((Reflector<Byte>) reflector)
        {
          @Override
          public Byte negate(Byte val)
          { return Integer.valueOf(-val).byteValue();
          }
        };
      }
      else if (clazz==Short.class)
      {
        translator=new NegateTranslator<Short>((Reflector<Short>) reflector)
        {
          @Override
          public Short negate(Short val)
          { return Integer.valueOf(-val).shortValue();
          }
        };
      }
      else if (clazz==Long.class)
      {
        translator=new NegateTranslator<Long>((Reflector<Long>) reflector)
        {
          @Override
          public Long negate(Long val)
          { return -(val);
          }
        };
      }
      else if (clazz==Double.class)
      {
        translator=new NegateTranslator<Double>((Reflector<Double>) reflector)
        {
          @Override
          public Double negate(Double val)
          { return -(val);
          }
        };
      }
      else if (clazz==java.math.BigDecimal.class)
      {
        translator=new NegateTranslator<BigDecimal>((Reflector<BigDecimal>) reflector)
        {
          @Override
          public BigDecimal negate(BigDecimal val)
          { return val.multiply(BigDecimal.valueOf(-1));
          }
        };
      }
      else if (clazz==java.math.BigInteger.class)
      {
        translator=new NegateTranslator<BigInteger>((Reflector<BigInteger>) reflector)
        {
          @Override
          public BigInteger negate(BigInteger val)
          { return val.multiply(BigInteger.valueOf(-1));
          }
        };
      }
      else
      { throw new BindException("Don't know how to negate a "+clazz);
      }
      
      _translatorMap.put(sourceBinding.getContentType(),translator);
    }
    
    return new TranslatorChannel<T,T>
      (focus.<T>bind(Expression.<T>create(_node))
      ,(Translator<T,T>) translator
      ,null
      );
  }
  
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Negative");
    prefix=prefix+"  ";
    _node.dumpTree(out,prefix);
  }

}

abstract class NegateTranslator<X>
  implements Translator<X,X>
{ 
  private Reflector<X> reflector;
  
  public NegateTranslator(Reflector<X> reflector)
  { this.reflector=reflector;
  }
  
  protected abstract X negate(X val);
  
  @Override
  public X translateForGet(X val,Channel<?>[] mods)
  { return negate(val);
  }
  
  @Override
  public X translateForSet(X val,Channel<?>[] mods)
  { return negate(val);
  }

  /**
   * Numeric negate is a function
   */
  @Override
  public boolean isFunction()
  { return true;
  }
  
  @Override
  public Reflector<X> getReflector()
  { return reflector;
  }
}