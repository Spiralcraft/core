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
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;

/**
 * <p>Makes a Contextual chainable. The Focus returned by the 
 *   bind() method of the specified Context will be chained into the Focus
 *   chain.
 * </p>
 * 
 * @author mike
 *
 */
public class ChainableContextualAdapter
  extends AbstractChainableContext
{
  
  private final Contextual delegate;
  
  public ChainableContextualAdapter(Contextual context)
  { this.delegate=context;
  }
  
  @Override
  public Focus<?> bindPeers(Focus<?> focusChain)
    throws ContextualException
  { return delegate.bind(focusChain);
  }  
  
  @Override
  public String toString()
  { return super.toString()+":"+delegate.toString();
  }
  
  @Override
  public DeclarationInfo getDeclarationInfo()
  { 
    if (declarationInfo==null && delegate instanceof Declarable)
    { return ((Declarable) delegate).getDeclarationInfo();
    }
    else
    { return super.getDeclarationInfo();
    }
  }
  
}