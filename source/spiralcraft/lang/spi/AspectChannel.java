//
// Copyright (c) 1998,2008 Michael Toth
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
import spiralcraft.lang.Reflector;

/**
 * <P>Exposes another aspect of a source object by using a different
 *   Reflector.
 * </P>
 * 
 * @author mike
 *
 */
public class AspectChannel<T>
  extends AbstractChannel<T>
  implements Channel<T>
{

  private final Channel<T> source;
  
  public AspectChannel(Reflector<T> reflector,Channel<T> source)
  { 
    super(reflector);
    this.source=source;
  }
  
  @Override
  protected T retrieve()
  { return source.get();
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { return source.set(val);
  }

  @Override
  public boolean isWritable()
  { return source.isWritable();
  }
  
  @Override
  public boolean isStatic()
  { return source.isStatic();
  }
  
}
