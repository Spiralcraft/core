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

import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.lang.PrimitiveReflector;
import spiralcraft.data.lang.TupleReflector;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.lang.spi.ClosureChannel;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;

import spiralcraft.log.Level;

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
  private boolean resolved;
  
  { setTransient(true);
  }
  
  public void setExpression(Expression<T> expression)
  { this.expression=expression;
  }
  
  public Expression<T> getExpression()
  { return expression;
  }
  
  @Override
  public boolean isDerived()
  { return true;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean isFunctionalEquivalent(Field<?> field)
  { 
    return
      field instanceof CalculatedFieldImpl
      && (expression!=null
          ?expression.equals(((CalculatedFieldImpl<T>) field).getExpression())
          :((CalculatedFieldImpl<T>) field).getExpression()==null
         )
      && super.isFunctionalEquivalent(field);
  }
  
  @Override
  void resolve()
    throws DataException
  {
    if (resolved)
    { return;
    }
    resolved=true;
    
    try
    {
      threadLocalBinding
        =new ThreadLocalChannel<Tuple>(TupleReflector.getInstance(getFieldSet()));
      SimpleFocus<Tuple> focus=new SimpleFocus<Tuple>(threadLocalBinding);
      threadLocalBinding.setContext(focus);
        
      expressionBinding=bindChannel(threadLocalBinding,focus,null);
    }
    catch (BindException x)
    { 
      if (debug && log.canLog(Level.DEBUG))
      {
        log.log
          (Level.DEBUG
          ,"CalculatedField "
            +getURI()
            +" deferring binding due to contextual dependencies"
          );
      }      
    }
      

    super.resolve();
    
  }
  
  /**
   * Called before resolve() when a definitive Type is needed.
   *    
   * @throws DataException
   */

  @SuppressWarnings({ "rawtypes", "unchecked"
    })
  @Override
  protected Type<T> resolveType()
    throws DataException
  { 
    if (expressionBinding!=null)
    { 
      Reflector<T> typeR=expressionBinding.getReflector();
      if (typeR instanceof DataReflector)
      { return ((DataReflector) typeR).getType();
      }
      else if (typeR instanceof PrimitiveReflector)
      { return ((PrimitiveReflector) typeR).getType();
      }
      else
      { return ReflectionType.canonicalType(typeR.getContentType());
      }
    }
    else
    { 
      try
      { bindChannel(threadLocalBinding,threadLocalBinding.getContext(),null);
      }
      catch (BindException x)
      {
        throw new DataException
          ("No type specified and unable to infer type for "+getURI(),x);
      }
    }
    return null;
  }
  
  @Override
  protected T getValueImpl(Tuple t)
  { 
    if (expressionBinding==null)
    { 
      if (debug && log.canLog(Level.FINE))
      {
        log.log
          (Level.FINE
          ,"CalculatedField "
            +getURI()
            +" has unresolved dependencies and must bound into a context"
          );
      }
      return null;
    }
    
    threadLocalBinding.push(t);
    try
    { 
      T ret=expressionBinding.get();
      return ret;
    }
    finally
    { threadLocalBinding.pop();
    }
    
  }
  
  @Override
  protected void setValueImpl(EditableTuple t,T val)
  { 
    if (expressionBinding==null)
    { 
      if (debug && log.canLog(Level.FINE))
      {
        log.log
          (Level.FINE
          ,"CalculatedField "
            +getURI()
            +" has unresolved dependencies and must be bound"
          );
      }
      return;
    }
    
    
    threadLocalBinding.push(t);
    try
    { expressionBinding.set(val);
    }
    finally
    { threadLocalBinding.pop();
    }
    
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<T> bindChannel
    (Channel<Tuple> source
    ,Focus<?> argFocus
    ,Expression<?>[] args
    )
    throws BindException
  { 
    if (expression==null)
    { 
      throw new BindException
        ("CalculatedField "+getURI()+" must have an expression");
    }
    
    
    // Use original binding context, never bind source expression in
    //   argument context
    Focus<?> context=source.getContext();
    if (context==null)
    { context=new SimpleFocus(source);
    }

    if (!context.isContext(source))
    { context=context.chain(source);
    }
    if (debug)
    { log.fine("Bound "+getURI()+" to "+context);
    }
    
    ClosureFocus<?> closure
      =new ClosureFocus(context,source);
    
    ClosureChannel ret= new ClosureChannel<T>(closure,closure.<T>bind(expression));
    ret.setDeclarationInfo(getDeclarationInfo());
    return ret;
  }
  
  
}
