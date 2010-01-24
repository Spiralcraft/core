//
// Copyright (c) 2010 Michael Toth
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

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

public class ExtendedReflector<T>
  extends AbstractReflector<T>
{

  protected final Reflector<T> baseReflector;

  public ExtendedReflector(Reflector<T> baseReflector)
  { this.baseReflector=baseReflector;
  }
  
  @Override
  public  <X> Channel<X> resolve
    (Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  { 
    if (name.startsWith("@"))
    { 
      Channel<X> ret=this.<X>resolveMeta(source,focus,name,params);
      if (ret!=null)
      { return ret;
      }
    }
    
    Channel<X> ret=baseReflector.resolve(source,focus,name,params);
    if (ret!=null)
    { 
      // Lookup name to find a Functor, which is bindable to
      //  source, focus, params
      
    
    }
    return ret;
    
  }
 

  @Override
  public URI getTypeURI()
  { return baseReflector.getTypeURI();
  }

  @Override
  public boolean isAssignableTo(
    URI typeURI)
  { return baseReflector.isAssignableTo(typeURI);
  }

  @Override
  public <D extends Decorator<T>> D decorate(
    Channel<T> source,
    Class<D> decoratorInterface)
    throws BindException
  { return baseReflector.<D>decorate(source,decoratorInterface);
  }

  @Override
  public Class<T> getContentType()
  { return baseReflector.getContentType();
  }



}
