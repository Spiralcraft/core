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
 * <p>Implements a unidirectional or bidirectional 
 *   coercion of one type to another. Bidirectional coercions may result in
 *   the loss of precision or data if the values used can only be represented
 *   in one type.
 * </p>
 * 
 * @author mike
 *
 */
public class CoercionChannel<S,T>
  extends SourcedChannel<S,T>
  implements Channel<T>
{

  private final Coercion<S,T> pullCoercion;
  private final Coercion<T,S> pushCoercion;
  private final boolean writable;

  
  public CoercionChannel
    (Reflector<T> reflector
    ,Channel<S> source
    ,Coercion<S,T> pullCoercion
    )
  { 
    super(reflector,source);
    this.pullCoercion=pullCoercion;
    this.pushCoercion=null;
    writable=false;
  }

  public CoercionChannel
    (Reflector<T> reflector
    ,Channel<S> source
    ,Coercion<S,T> pullCoercion
    ,Coercion<T,S> pushCoercion
    )
  { 
    super(reflector,source);
    this.pullCoercion=pullCoercion;
    this.pushCoercion=pushCoercion;
    writable=pushCoercion!=null && !source.isConstant(); 
  }
  
  @Override
  protected T retrieve()
  { return pullCoercion.coerce(source.get());
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { 
    if (pushCoercion!=null)
    { return source.set(pushCoercion.coerce(val));
    }
    else
    { return false;
    }
  }

  @Override
  public boolean isWritable()
  { return writable && source.isWritable();
  }
  
  @Override
  public boolean isConstant()
  { return source.isConstant();
  }
  
}
