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
import spiralcraft.data.task.Session;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.task.Eval;
import spiralcraft.task.Scenario;
import spiralcraft.util.URIUtil;

/**
 * A method which evaluates an Expression in the context of a Tuple of a given 
 *   type and in the scope of a Transaction.
 * 
 * @author mike
 *
 * @param <T>
 * @param <C>
 * @param <R>
 */
public class TransactionMethod<T,C,R>
  extends AbstractTaskMethod<T,C,R>
{

  private Expression<R> x;
  private Expression<C> contextX;
  private boolean requireTransaction=true;
  private boolean isolateTransaction=false;
  private boolean inheritDataSession=true;

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
    
    Session<C> session=new Session<C>();
    session.setDebug(true);
    session.setContextX(contextX);
    session.setContextAliasURI
      (URIUtil.addPathSuffix(this.getDataType().getURI(),"_"+this.getName()));
    session.setTransactional(requireTransaction);
    session.setIsolate(isolateTransaction);
    session.setInherit(inheritDataSession);
    session.setAddChainResult(true);
    
    
    Eval<C,R> eval=new Eval<C,R>(null,x);
    eval.setDebug(true);
    session.setChain(eval);
    
    try
    { 
      Focus<?> sessionFocus=session.bind(context);
      log.fine("TransactionMethod bound "+sessionFocus);
      return (Focus) 
        LangUtil.findFocus(Session.class,sessionFocus);
    }
    catch (ContextualException x)
    { throw new BindException("Error binding TransactionMethod ",x);
    }

  }
  
       
}
  


