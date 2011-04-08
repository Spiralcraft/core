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
package spiralcraft.lang.spi;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

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
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  { 
    local=new ThreadLocalChannel<T>(reflector,inheritable);
    return super.bind(focusChain.chain(local));
  }

  public void set(T val)
  { local.set(val);
  }
  
  public T get()
  { return local.get();
  }
  
  @Override
  public void push()
  { 
    local.push();
    super.push();
  }

  @Override
  public void pop()
  {
    super.pop();
    local.pop();
  }

  
}
