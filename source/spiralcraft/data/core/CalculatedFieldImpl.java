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


/**
 * A Field which provides a value based on an Expression.
 * 
 * @author mike
 */
public class CalculatedFieldImpl<T>
  extends FieldImpl<T>
{

  private ThreadLocalChannel<Tuple> threadLocalBinding;
  private Channel<T> expressionBinding;
  private Expression<T> expression;
  
  { setTransient(true);
  }
  
  public void setExpression(Expression<T> expression)
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
      expressionBinding=bindChannel(focus);
    }
    catch (BindException x)
    { throw new DataException("Error resolving Field "+getURI()+": "+x,x);
    }
    
  }
  
  @Override
  protected T getValueImpl(Tuple t)
  { 
    threadLocalBinding.push(t);
    try
    { 
      T ret=expressionBinding.get();
//      System.err.println
//        ("CalculatedFieldImpl: "+expression.toString()+": "+ret);
      return ret;
    }
    finally
    { threadLocalBinding.pop();
    }
    
  }
  
  @Override
  protected void setValueImpl(EditableTuple t,T val)
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
  public Channel<T> bindChannel
    (Focus<Tuple> focus)
    throws BindException
  { return focus.<T>bind(expression);
  }
  
}
