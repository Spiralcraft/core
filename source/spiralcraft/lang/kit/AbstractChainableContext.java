//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.lang.kit;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Context;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;

/**
 * <p>Provides support for implementing ChainableContext
 * </p>
 * 
 * @author mike
 *
 */
public class AbstractChainableContext
  implements ChainableContext
{

  public static final ChainableContext createChain(Contextual chain)
  {
    if (!(chain instanceof Context))
    { return new ChainableContextualAdapter(chain);
    }
    else if (!(chain instanceof ChainableContext))
    { return new ChainableContextAdapter((Context) chain);
    }
    else
    { return (ChainableContext) chain;
    }
  }

  protected final ClassLog log=ClassLog.getInstance(getClass());

  private Contextual next;
  private boolean context;
  private boolean chainable;
  
  public AbstractChainableContext()
  {
    
  }

  public AbstractChainableContext(Context next)
    throws BindException
  { chain(next);
  }
  
  @Override
  public final void push()
  { 
    pushLocal();
    if (context)
    { ((Context) next).push();
    }
  }

  @Override
  public final void pop()
  {
    if (context)
    { ((Context) next).pop();
    }
    popLocal();
  }

  protected void pushLocal()
  {
  }
  
  protected void popLocal()
  {
  }
  
  /**
   * Override to bind any dependencies on external context used to
   *   estabish the local context.
   * 
   * @param focusChain
   * @return
   * @throws BindException
   */
  protected Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }
  
  /**
   * Override to bind any dependencies on the local context before the
   *   next context in the chain is bound.
   * 
   * @param focusChain
   * @return
   * @throws BindException
   */
  protected Focus<?> bindPeers(Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }
  
  @Override
  public final Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    focusChain=bindImports(focusChain);
    pushLocal();
    try
    { 
      focusChain=bindPeers(focusChain);
      if (next!=null)
      { return next.bind(focusChain);
      }
      else
      { return focusChain;
      }
    }
    finally
    { popLocal();
    }
  }

  @Override
  public ChainableContext chain(Contextual chain)
  {
    if (chain==null)
    { throw new IllegalArgumentException("Contextual to chain cannot be null");
    }
    
    if (next!=null)
    { 
      if (chainable)
      { return ((ChainableContext) next).chain(chain);
      }
      else
      { throw new IllegalStateException("Chain already sealed with "+next);
      }
    }
    else
    { 
      if (!(chain instanceof Context))
      { 
        chain=new ChainableContextualAdapter(chain);
        context=false;
      }
      else if (!(chain instanceof ChainableContext))
      { 
        chain=new ChainableContextAdapter((Context) chain);
        context=true;
      }
      next=chain;
      chainable=true;
    }
    
    if (chain instanceof ChainableContext)
    { return (ChainableContext) chain;
    }
    else
    { return null;
    }
        
  }
  
  @Override
  public void seal(Contextual last)
  {
    if (next!=null)
    { 
      if (chainable)
      { ((ChainableContext) next).seal(last);
      }
      else
      { throw new IllegalStateException("Chain already sealed with: "+next);
      }
    }
    else
    { next=last;
    }
    
  }

}

