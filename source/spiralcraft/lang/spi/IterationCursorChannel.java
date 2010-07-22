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
package spiralcraft.lang.spi;


import spiralcraft.lang.Channel;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.Reflector;


/**
 * <P>Provides access to an object at the current position of an iteration,
 *   expressed by the IterationCursor returned by the specified Channel.
 */
public class IterationCursorChannel<T>
  extends SourcedChannel<IterationCursor<T>,T>
{
  
  /**
   * Create a new IterationCursorBinding which obtains the value of the
   *   current iteration position from the IterationCursor obtained from
   *   the specified source.
   */
  public IterationCursorChannel
    (Reflector<T> componentReflector,Channel<IterationCursor<T>> source)
  { super(componentReflector,source);
  }
  
 
  @Override
  public boolean isWritable()
  { return false;
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
