//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.security.auth;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Channel;

import spiralcraft.lang.spi.AbstractChannel;

import java.net.URI;
import java.util.Map;

/**
 * Provides spiralcraft.lang package access to the credential set of
 *   an AuthSession for the implementation of application specific tasks.
 */
public class CredentialSetChannel
  extends AbstractChannel<Map<String,Credential<?>>>
{
  private Map<String,Credential<?>> map;
  
  public CredentialSetChannel(Map<String,Credential<?>> map)
  {
    super(new CredentialMapReflector(map));
    this.map=map;
  }

  @Override
  protected Map<String, Credential<?>> retrieve()
  { return map;
  }

  @Override
  protected boolean store(Map<String, Credential<?>> val)
  { return false;
  }
  
  
}

@SuppressWarnings("unchecked") // Complex to genericize
class CredentialMapReflector
  implements Reflector<Map<String,Credential<?>>>
{
  // XXX Consider the use of namespaces, now that they are implemented
  
  private Map<String,Credential<?>> map;
  
  public CredentialMapReflector(Map<String,Credential<?>> map)
  { this.map=map;
  }
  
  
  public Decorator decorate(Channel source, Class decoratorInterface)
    throws BindException
  { return null;
  }

  
  public Class getContentType()
  { return Map.class;
  }

  public <X> Channel<X> resolve
    (Channel<Map<String,Credential<?>>> source
    , Focus<?> focus
    , String name
    , Expression<?>[] params
    ) throws BindException
  {
    if (map.get(name)==null)
    { 
      throw new BindException
        ("Credential map does not contain credential '"+name+"'");
    }
    return new CredentialChannel(map,name);

  }

  @Override
  public URI getTypeURI()
  {
    // Undefined
    return null;
  }


  @Override
  public boolean isAssignableTo(URI typeURI)
  {
    // Undefined
    return false;
  } 
  
}
