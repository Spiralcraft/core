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
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>A Context which publishes a value into the FocusChain.
 * </p>
 *   
 * 
 * @author mike
 *
 */
public class ThreadLocalContext<T>
  extends AbstractChainableContext
{

  private final Reflector<T> reflector;
  private ThreadLocalChannel<T> local;
  protected boolean inheritable;
  
  public ThreadLocalContext(Reflector<T> reflector)
  { this.reflector=reflector;
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  { 
    local=new ThreadLocalChannel<T>(reflector,inheritable);
    return super.bindImports(focusChain.chain(local));
  }

  public void set(T val)
  { local.set(val);
  }
  
  public T get()
  { return local.get();
  }
  
  @Override
  protected void pushLocal()
  { local.push();
  }

  @Override
  protected void popLocal()
  { local.pop();
  }

  
}
