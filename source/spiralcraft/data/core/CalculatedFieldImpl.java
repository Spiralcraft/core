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
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import spiralcraft.data.lang.TupleReflector;

import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;

@SuppressWarnings("unchecked")
/**
 * A Field which provides a value based on an Expression.
 * 
 * @author mike
 */
public class CalculatedFieldImpl
  extends FieldImpl
{

  private ThreadLocalChannel<Tuple> threadLocalBinding;
  private Channel expressionBinding;
  private Expression<?> expression;
  
  public void setExpression(Expression<?> expression)
  { this.expression=expression;
  }
  
  @Override
  void resolve()
    throws DataException
  {
    super.resolve();
    try
    {
      threadLocalBinding
        =new ThreadLocalChannel<Tuple>(TupleReflector.getInstance(getFieldSet()));
      SimpleFocus<Tuple> focus=new SimpleFocus<Tuple>(threadLocalBinding);
      expressionBinding=bind(focus);
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
  
  @Override
  protected void setValueImpl(EditableTuple t,Object val)
  { 
    threadLocalBinding.push(t);
    try
    { expressionBinding.set(val);
    }
    finally
    { threadLocalBinding.pop();
    }
    
  }
  
  @Override
  public Channel bind
    (Focus<? extends Tuple> focus)
    throws BindException
  { return focus.bind(expression);
  }
  
}
