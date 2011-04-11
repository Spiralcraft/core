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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;

/**
 * <p>Presents a constant literal object, either set explicitly or resolved
 *   from a source channel at construction time.
 * </p>
 * 
 * @author mike
 *
 */
public class ConstantChannel<T>
  extends AbstractChannel<T>
{
  private final T value;
  
  public static <T> Channel<T> forBean(T bean)
    throws BindException
  { 
    return new ConstantChannel<T>
      (BeanReflector.<T>getInstance(bean.getClass()),bean);
  }
  
  public ConstantChannel(Channel<T> source)
  { 
    super(source.getReflector());
    value=source.get();
  }

  public ConstantChannel(Reflector<T> reflector,T value)
  { 
    super(reflector);
    this.value=value;
  }

  @Override
  protected T retrieve()
  { return value;
  }

  @Override
  protected boolean store(T val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }
  
  @Override
  public boolean isConstant()
  { return true;
  }
  
}
