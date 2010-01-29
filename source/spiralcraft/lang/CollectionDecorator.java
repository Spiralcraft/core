//
// Copyright (c) 2009 Michael Toth
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


public abstract class CollectionDecorator<T,I>
  extends IterationDecorator<T,I>
{

  public CollectionDecorator(Channel<T> source,Reflector<I> componentReflector)
  { super(source,componentReflector);
  }
  
  /**
   * Create a new Collection of the appropriate type and content type
   * 
   * @return A new collection
   */
  public abstract T newCollection();
  
  /**
   * <p>Add an item to the collection created by newCollection and return
   *   the collection containing the added element.
   * </p>
   * 
   * <p>The returned collection may be the same collection that was passed
   *   in, or a new collection if the specified collection does not allow
   *   dynamic resizing (eg. arrays).
   * </p>
   * 
   * @param collection
   * @param item
   */
  public abstract T add(T collection,I item);
  
  
  /**
   * Obtain the size of the collection
   * 
   * @param collection
   * @return
   */
  public abstract int size(T collection);
}
