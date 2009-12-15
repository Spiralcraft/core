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

import java.net.URI;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Channel;


/**
 * Type that is undefined (ie. null)
 */
public class VoidReflector
  extends AbstractReflector<Void>
{

  URI uri=URI.create("class:/java/lang/Void");
  
  /**
   * Generate a new Binding which resolves the name and the given parameter 
   *   expressions against the source Binding within the context of the supplied
   *   Focus.
   */
  @Override
  public <X> Channel<X> resolve
    (Channel<Void> source,Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { 
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
    
    // We should implement .equals()
    return null;
  }

  /**
   * Decorate the specified optic with a decorator that implements the
   *   specified interface
   */
  @Override
  public <D extends Decorator<Void>> D decorate
      (Channel<Void> source
      ,Class<D> decoratorInterface
      )
    throws BindException
  { return null;
  }
  
  /**
   * Return the Java class of the data object accessible through Bindings 
   *   associated with this Reflector
   */
  @Override
  public Class<Void> getContentType()
  { return Void.class;
  }


  @Override
  public URI getTypeURI()
  { return uri;
  }

  @Override
  public boolean isAssignableTo
    (URI typeURI)
  {
    // Void is not assignable to anything
    return false;
  }
}
