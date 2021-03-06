//
// Copyright (c) 2010 Michael Toth
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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

public class ClosureChannel<T>
  extends AbstractChannel<T>
{
  private final ClosureFocus<?> closure;
  private final Channel<T> enclosedChannel;
  
  public ClosureChannel(ClosureFocus<?> closure,Channel<T> enclosedChannel)
  { 
    super(enclosedChannel.getReflector());
    this.enclosedChannel=enclosedChannel;
    this.closure=closure;
    this.context=closure;
  }
  
  @Override
  protected T retrieve()
  {
    closure.push();
    try
    { return enclosedChannel.get();
    }
    finally
    { closure.pop();
    }
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  {
    closure.push();
    try
    { return enclosedChannel.set(val);
    }
    finally
    { closure.pop();
    }
  }

  @Override
  public String toString()
  { return super.toString()+" -> "+enclosedChannel.toString();
  }
}
