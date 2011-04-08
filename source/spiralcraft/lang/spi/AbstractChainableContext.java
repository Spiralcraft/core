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
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Context;
import spiralcraft.lang.Focus;

public class AbstractChainableContext
  implements ChainableContext
{

  private Contextual next;
  private boolean context;
  private boolean chainable;
  
  @Override
  public void push()
  { 
    if (context)
    { ((Context) next).push();
    }
  }

  @Override
  public void pop()
  {
    if (context)
    { ((Context) next).pop();
    }
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return next.bind(focusChain);
  }

  @Override
  public ChainableContext chain(
    Contextual chain)
    throws BindException
  {
    if (next!=null)
    { 
      if (chainable)
      { ((ChainableContext) next).chain(chain);
      }
      else
      { throw new BindException("Not chainable: "+next);
      }
    }
    else
    { next=chain;
    }
    if (chain instanceof ChainableContext)
    { return (ChainableContext) chain;
    }
    else
    { return null;
    }
        
  }

}
