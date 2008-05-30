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

import java.util.Iterator;

/**
 * Supports Iteration through containers and data structures with multiple
 *   elements.
 *
 * The iterator() method provides an IterationCursor which exposes the data at
 *   the current position of the iteration.
 */
public abstract class IterationDecorator<T,I>
  implements Decorator<T>,Iterable<I>
{   
  
  protected final Channel<T> source;
  private final Reflector<I> componentReflector;
  
  public IterationDecorator(Channel<T> source,Reflector<I> componentReflector)
  { 
    this.source=source;
    this.componentReflector=componentReflector;
  }
  
  /**
   * 
   * @return The Reflector which describes the component type of the Iteration.
   *   
   */
  public Reflector<I> getComponentReflector()
  { return componentReflector;
  }
  
  /**
   * Override to provide an implementation specific Iterator
   * 
   * @return
   */
  protected abstract Iterator<I> createIterator();
  
  /**
   * <p>Create a new IterationCursor that holds the state of a new iteration
   * </p>
   */
  public IterationCursor<I> iterator()
  { return new IterationCursor<I>(createIterator());
  }
  
  
  
}
