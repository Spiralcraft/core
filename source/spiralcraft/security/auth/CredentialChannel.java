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

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.BeanReflector;

import java.util.Map;

/**
 * A Binding which pulls a named Credential out of a shared map and retrieves
 *   its current value
 *   
 * @author mike
 *
 */
public class CredentialChannel<T>
  extends AbstractChannel<T>
{
  private final String name;
  private final Map<String,Credential<?>> map;

  @SuppressWarnings("unchecked") // Heterogeneous map
  public CredentialChannel(Map<String,Credential<?>> map,String name)
    throws BindException
  { 
    super
      ((BeanReflector<T>) BeanReflector
          .getInstance((map.get(name).getTokenType()))
      );
    this.name=name;
    this.map=map;
  }
  
  @SuppressWarnings("unchecked") // Heterogeneous map
  @Override
  protected T retrieve()
  { return (T) map.get(name).getValue();
  }

  @Override
  protected boolean store(Object val)
  { return false;
  }
}
