//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang.optics;


import spiralcraft.lang.Optic;
import spiralcraft.lang.IterationContext;

import spiralcraft.lang.optics.Prism;

/**
 * <P>Provides access to an object at the current position of an iteration,
 *   expressed by the IterationContext returned by the specified Optic.
 */
public class IterationContextBinding<T>
  extends AbstractBinding<T>
{
  private final Optic<IterationContext<T>> source;
  
  /**
   * Create a new IterationContextBinding which obtains the value of the
   *   current iteration position from the IterationContext obtained from
   *   the specified source.
   */
  public IterationContextBinding
    (Prism<T> componentPrism,Optic<IterationContext<T>> source)
  { 
    super(componentPrism);
    this.source=source;
  }
  
 
  @Override
  public T retrieve()
  { return source.get().getValue();
  }

  @Override
  public boolean store(T val)
  { return false;
  }

}
