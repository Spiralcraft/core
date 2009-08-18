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

import java.net.URI;

/**
 * Wraps another reflector to add or extend functionality
 */
public class Extender<T>
  extends Reflector<T>
{

  protected final Reflector<T> base;
  
  public Extender(Reflector<T> base)
  { this.base=base;
  }
  
  @Override
  public <D extends Decorator<T>> D decorate(
    Channel<T> source,
    Class<D> decoratorInterface)
    throws BindException
  { return base.decorate(source,decoratorInterface);
  }

  @Override
  public Class<T> getContentType()
  { return base.getContentType();
  }

  @Override
  public URI getTypeURI()
  { return base.getTypeURI();
  }

  @Override
  public boolean isAssignableTo(
    URI typeURI)
  { return base.isAssignableTo(typeURI);
  }

  @Override
  public <X> Channel<X> resolve(
    Channel<T> source,
    Focus<?> focus,
    String name,
    Expression<?>[] params)
    throws BindException
  { return base.resolve(source, focus, name, params);
  }

}
