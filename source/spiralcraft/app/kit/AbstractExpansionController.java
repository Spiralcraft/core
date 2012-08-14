//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.kit;

import spiralcraft.app.Component;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

/**
 * <p>Manages a state subtree for for each item in an input set
 * </p>
 * 
 * @author mike
 *
 */
public class 
  AbstractExpansionController<C,T>
  extends AbstractController<ExpansionState<T>>
{
  // If no children, we still want to run the expansion logic
  { createEmptyContainer=true;
  }
  
  protected Channel<C> collection;
  
  /**
   * Create the container for child components. Defaults to creating a
   *   StandardContainer.
   * 
   * @param children
   * @return
   */
  @Override
  protected ExpansionContainer<C,T> createChildContainer(Component[] children)
  { 
    ExpansionContainer<C,T> container
      =new ExpansionContainer<C,T>(this,children);
    container.setLog(log);
    container.setLogLevel(logLevel);
    return container;
  }  
  
  @SuppressWarnings("rawtypes")
  @Override
  protected Class<ExpansionState> getStateClass()
  { return ExpansionState.class;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> chain) 
    throws ContextualException
  {
    chain=super.bindExports(chain);
    collection=resolveCollection(chain);
    return chain.chain(collection);
  }
  
  @SuppressWarnings("unchecked")
  protected Channel<C> resolveCollection(Focus<?> chain)
  { return (Channel<C>) chain.getSubject();
  }
  
}
