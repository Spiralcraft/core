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
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.lang.BindException;
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Context;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Provides support for implementing ChainableContext
 * </p>
 * 
 * @author mike
 *
 */
public class AbstractChainableContext
  implements ChainableContext,Declarable
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
  protected Level logLevel
    =ClassLog.getInitialDebugLevel(getClass(),Level.INFO);

  private Contextual next;
  private boolean context;
  private boolean chainable;
  protected DeclarationInfo declarationInfo;
  
  public AbstractChainableContext()
  {
    
  }

  public AbstractChainableContext(Context next)
    throws BindException
  { chain(next);
  }
  
  public void setLogLevel(Level logLevel)
  { this.logLevel=logLevel;
  }
  
  @Override
  public final void push()
  { 
//    if (logLevel.isFine())
//    { log.fine("Pushing "+this+" (next is "+next+")");
//    }
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
//    if (logLevel.isFine())
//    { log.fine("Popped "+this);
//    }
  }

  protected void pushLocal()
  {
  }
  
  protected void popLocal()
  {
  }
  
  /**
   * Override to bind any dependencies on external context used to
   *   establish the local context.
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

  /**
   * Override to bind any dependencies on the last chain in the context. The
   *   Focus returned will be returned by the bind() method for the chain.
   * 
   * @param focusChain
   * @return
   * @throws BindException
   */
  protected Focus<?> bindExports(Focus<?> focusChain)
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
      { focusChain=next.bind(focusChain);
      }
      return bindExports(focusChain);
    }
    catch (ContextualException x)
    { 
      if (getDeclarationInfo()!=null)
      { 
        throw new ContextualException
          ("Error binding "+toString()
          ,getDeclarationInfo().getLocation()!=null
          ?getDeclarationInfo().getLocation()
          :getDeclarationInfo().getDeclaredType()
          ,x
          );
      }  
      else
      { throw new ContextualException("Error binding "+toString(),x);
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
      { chain=new ChainableContextualAdapter(chain);
      }
      else if (!(chain instanceof ChainableContext))
      { chain=new ChainableContextAdapter((Context) chain);
      }
      next=chain;
      chainable=true;
      context=true;
    }
    
    if (chain instanceof ChainableContext)
    { return (ChainableContext) chain;
    }
    else
    { return null;
    }
        
  }
  
  @Override
  public void insertNext(ChainableContext insert)
  {
    if (insert==null)
    { 
      throw new IllegalArgumentException
        ("Contextual to insertNext cannot be null");
    }

    if (next!=null)
    { insert.chain(next);
    }
    next=insert; 
    chainable=true;
    context=true;
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
    { 
      next=last;
      if (next instanceof Context)
      { context=true;
      }
    }
    
  }
  
  public Contextual getNext()
  { return next;
  }

  @Override
  public void setDeclarationInfo(
    DeclarationInfo declarationInfo)
  { this.declarationInfo=declarationInfo;
  }

  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
  }

}

