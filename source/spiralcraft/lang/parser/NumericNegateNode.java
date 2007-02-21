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

public class NumericNegateNode
  extends Node
{

  private final Node _node;

  public static HashMap<Class,NegateLense> _lenseMap
    =new HashMap<Class,NegateLense>();
  
  
  public NumericNegateNode(Node node)
  {  _node=node;
  }

  
  public Optic bind(Focus focus)
    throws BindException
  {
    Optic sourceBinding=focus.bind(new Expression(_node,null));
    if (!Number.class.isAssignableFrom(sourceBinding.getContentType()))
    { throw new BindException("Negation operator only applies to numbers");
    }

    NegateLense lense=_lenseMap.get(sourceBinding.getContentType());
    Prism prism=sourceBinding.getPrism();
    
    if (lense==null)
    { 
      Class clazz=sourceBinding.getContentType();
      if (clazz==Integer.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return -((Integer) val);
          }
        };
      }
      else if (clazz==Float.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return -((Float) val);
          }
        };
      }
      else if (clazz==Byte.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return -((Byte) val);
          }
        };
      }
      else if (clazz==Short.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return -((Short) val);
          }
        };
      }
      else if (clazz==Long.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return -((Long) val);
          }
        };
      }
      else if (clazz==Double.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return -((Double) val);
          }
        };
      }
      else if (clazz==java.math.BigDecimal.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return ((BigDecimal) val).multiply(BigDecimal.valueOf(-1));
          }
        };
      }
      else if (clazz==java.math.BigInteger.class)
      {
        lense=new NegateLense(prism)
        {
          public Object negate(Object val)
          { return ((BigInteger) val).multiply(BigInteger.valueOf(-1));
          }
        };
      }
      else
      { throw new BindException("Don't know how to negate a "+clazz);
      }
      
      _lenseMap.put(sourceBinding.getContentType(),lense);
    }
    
    return new LenseBinding
      (focus.bind(new Expression(_node,null))
      ,lense
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

abstract class NegateLense
  implements Lense
{ 
  private Prism prism;
  
  public NegateLense(Prism prism)
  { this.prism=prism;
  }
  
  protected abstract Object negate(Object val);
  
  public Object translateForGet(Object val,Optic[] mods)
  { return negate(val);
  }
  
  public Object translateForSet(Object val,Optic[] mods)
  { return negate(val);
  }

  public Prism getPrism()
  { return prism;
  }
}