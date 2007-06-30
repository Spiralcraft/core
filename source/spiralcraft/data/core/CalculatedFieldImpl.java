//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.core;

import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;

import spiralcraft.data.lang.TupleReflector;

import spiralcraft.lang.spi.ThreadLocalBinding;

import spiralcraft.lang.BindException;
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;

/**
 * A Field which provides a value based on an Expression.
 * 
 * @author mike
 */
public class CalculatedFieldImpl
  extends FieldImpl
{

  private ThreadLocalBinding<Tuple> threadLocalBinding;
  private Channel expressionBinding;
  private Expression<?> expression;
  
  public void setExpression(Expression<?> expression)
  { this.expression=expression;
  }
  
  void resolve()
    throws DataException
  {
    super.resolve();
    try
    {
      threadLocalBinding
        =new ThreadLocalBinding<Tuple>(TupleReflector.getInstance(getFieldSet()));
      DefaultFocus<Tuple> focus=new DefaultFocus<Tuple>(threadLocalBinding);
      expressionBinding=focus.bind(expression);
    }
    catch (BindException x)
    { throw new DataException("Error resolving Field "+getURI()+": "+x,x);
    }
    
  }
  
  @Override
  protected Object getValueImpl(Tuple t)
  { 
    threadLocalBinding.push(t);
    try
    { 
      Object ret=expressionBinding.get();
//      System.err.println
//        ("CalculatedFieldImpl: "+expression.toString()+": "+ret);
      return ret;
    }
    finally
    { threadLocalBinding.pop();
    }
    
  }
}
