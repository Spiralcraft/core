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
package spiralcraft.lang.kit;

import spiralcraft.common.Coercion;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * <p>Implements a unidirectional coercion of one type to another
 * </p>
 * 
 * @author mike
 *
 */
public class CoercionChannel<S,T>
  extends SourcedChannel<S,T>
  implements Channel<T>
{

  private final Coercion<S,T> coercion;

  
  public CoercionChannel(Reflector<T> reflector,Channel<S> source,Coercion<S,T> coercion)
  { 
    super(reflector,source);
    this.coercion=coercion;
  }
  
  @Override
  protected T retrieve()
  { return coercion.coerce(source.get());
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { return false;
  }

  @Override
  public boolean isWritable()
  { return false;
  }
  
  @Override
  public boolean isConstant()
  { return source.isConstant();
  }
  
}
