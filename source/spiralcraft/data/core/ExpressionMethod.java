//
// Copyright (c) 2009 Michael Toth
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

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;

import spiralcraft.task.Eval;
import spiralcraft.task.Scenario;

/**
 * A method which evaluates an Expression in the context of a Tuple of a given 
 *   type
 * 
 * @author mike
 *
 * @param <T>
 * @param <C>
 * @param <R>
 */
public class ExpressionMethod<T,C,R>
  extends AbstractTaskMethod<T,C,R>
{

  private Expression<R> x;
  private Expression<C> contextX;
  
  public void setX(Expression<R> x)
  { this.x=x;
  }
  
  public void setContextX(Expression<C> contextX)
  { this.contextX=contextX;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes"})
  @Override
  public Focus<Scenario<C,R>> bindTask(
    Focus<?> context,
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  {
    

    
    if (x==null)
    { throw new BindException("Missing expression in method: "+this);
    }
    
    Eval<C,R> eval=new Eval<C,R>(contextX,x);
    try
    { return (Focus) 
        LangUtil.findFocus(Eval.class,eval.bind(context));
    }
    catch (ContextualException x)
    { throw new BindException("Error binding ExpressionMethod ",x);
    }

  }
  
       
}
  


