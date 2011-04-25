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

import spiralcraft.lang.Context;

/**
 * <p>Makes a non-chainable Context chainable. The Focus returned by the 
 *   bind() method of the specified Context will be chained into the Focus
 *   chain.
 * </p>
 * 
 * @author mike
 *
 */
public class ChainableContextAdapter
  extends AbstractChainableContext
{
  private final Context delegate;
  
  public ChainableContextAdapter(Context context)
  { this.delegate=context;
  }
  
  @Override
  protected void pushLocal()
  { delegate.push();
  }
  
  @Override
  protected void popLocal()
  { delegate.pop();
  }
  

}