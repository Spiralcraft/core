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
package spiralcraft.lang;

import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.IterationContextBinding;

import java.util.Iterator;

/**
 * Supports Iteration through containers and data structures with multiple
 *   elements.
 *
 * The iterator() method provides an IterationContext which exposes the data at
 *   the current position of the iteration.
 */
public abstract class IterationDecorator<T,I>
  implements Decorator<T>,Iterable
{   
  
  protected final Optic<T> source;
  private final Prism<I> componentPrism;
  
  public IterationDecorator(Optic<T> source,Prism<I> componentPrism)
  { 
    this.source=source;
    this.componentPrism=componentPrism;
  }
  
  /**
   * 
   * @return The Prism which describes the component type of the Iteration.
   *   Use with the IterationContextBinding
   *   use with 
   */
  public IterationContextBinding<I>
    createComponentBinding(Optic<IterationContext<I>> iterationContextSource)
  { 
    return new IterationContextBinding<I>
      (componentPrism,iterationContextSource);
  }
  
  protected abstract Iterator<I> createIterator();
  
  /**
   * <P>Create a new IterationContext that holds the state of an iteration
   */
  public IterationContext<I> iterator()
  { return new IterationContext<I>(createIterator());
  }
  
  
  
}
