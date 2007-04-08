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

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;

import spiralcraft.util.lang.ClassUtil;

public class NumericNegateNode<T extends Number>
  extends Node
{

  private final Node _node;

  public static HashMap<Class,NegateLense<?>> _lenseMap
    =new HashMap<Class,NegateLense<?>>();
  
  
  public NumericNegateNode(Node node)
  {  _node=node;
  }

  // Suppress Warnings notes:
  //   Lense cast at end of method from specific back to T- type checked against
  //   content type in type selector. Also Prism is cast up to specific type, also safe
  //   due to API. There might be a cleaner way to do this. This class is heterogeneous.
  @SuppressWarnings("unchecked")
  public Optic<T> bind(Focus<?> focus)
    throws BindException
  {
    Optic<T> sourceBinding=focus.<T>bind(new Expression<T>(_node,null));
    if (!Number.class.isAssignableFrom(ClassUtil.boxedEquivalent(sourceBinding.getContentType())))
    { throw new BindException("Negation operator only applies to numbers, not "+sourceBinding.getContentType());
    }

    NegateLense<? extends Number> lense=(NegateLense<T>) _lenseMap.get(sourceBinding.getContentType());
    Prism<T> prism=sourceBinding.getPrism();
    
    if (lense==null)
    { 
      Class clazz=ClassUtil.boxedEquivalent(sourceBinding.getContentType());
      if (clazz==Integer.class)
      {
        
        lense=new NegateLense<Integer>((Prism<Integer>) prism)
        {
          public Integer negate(Integer val)
          { return -val;
          }
        };
      }
      else if (clazz==Float.class)
      {
        lense=new NegateLense<Float>((Prism<Float>) prism)
        {
          public Float negate(Float val)
          { return -val;
          }
        };
      }
      else if (clazz==Byte.class)
      {
        lense=new NegateLense<Byte>((Prism<Byte>) prism)
        {
          public Byte negate(Byte val)
          { return Integer.valueOf(-val).byteValue();
          }
        };
      }
      else if (clazz==Short.class)
      {
        lense=new NegateLense<Short>((Prism<Short>) prism)
        {
          public Short negate(Short val)
          { return Integer.valueOf(-val).shortValue();
          }
        };
      }
      else if (clazz==Long.class)
      {
        lense=new NegateLense<Long>((Prism<Long>) prism)
        {
          public Long negate(Long val)
          { return -(val);
          }
        };
      }
      else if (clazz==Double.class)
      {
        lense=new NegateLense<Double>((Prism<Double>) prism)
        {
          public Double negate(Double val)
          { return -(val);
          }
        };
      }
      else if (clazz==java.math.BigDecimal.class)
      {
        lense=new NegateLense<BigDecimal>((Prism<BigDecimal>) prism)
        {
          public BigDecimal negate(BigDecimal val)
          { return val.multiply(BigDecimal.valueOf(-1));
          }
        };
      }
      else if (clazz==java.math.BigInteger.class)
      {
        lense=new NegateLense<BigInteger>((Prism<BigInteger>) prism)
        {
          public BigInteger negate(BigInteger val)
          { return val.multiply(BigInteger.valueOf(-1));
          }
        };
      }
      else
      { throw new BindException("Don't know how to negate a "+clazz);
      }
      
      _lenseMap.put(sourceBinding.getContentType(),lense);
    }
    
    return new LenseBinding<T,T>
      (focus.<T>bind(new Expression<T>(_node,null))
      ,(Lense<T,T>) lense
      ,null
      );
  }
  
  
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Negative");
    prefix=prefix+"  ";
    _node.dumpTree(out,prefix);
  }

}

abstract class NegateLense<X>
  implements Lense<X,X>
{ 
  private Prism<X> prism;
  
  public NegateLense(Prism<X> prism)
  { this.prism=prism;
  }
  
  protected abstract X negate(X val);
  
  public X translateForGet(X val,Optic[] mods)
  { return negate(val);
  }
  
  public X translateForSet(X val,Optic[] mods)
  { return negate(val);
  }

  public Prism<X> getPrism()
  { return prism;
  }
}