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
import spiralcraft.lang.Channel;

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
  private final Channel<Map<String,Credential<?>>> mapChannel;

  @SuppressWarnings("unchecked") // Heterogeneous map
  public CredentialChannel
    (Channel<Map<String,Credential<?>>> mapChannel,Class tokenType,String name)
    throws BindException
  { 
    super
      ((BeanReflector<T>) BeanReflector
          .getInstance(tokenType)
      );
    this.name=name;
    this.mapChannel=mapChannel;
  }
  
  @SuppressWarnings("unchecked") // Heterogeneous map
  @Override
  protected T retrieve()
  { 
    Map<String,Credential<?>> map=mapChannel.get();
    if (map!=null)
    { 
      Credential<T> credential=(Credential<T>) map.get(name);
      if (credential!=null)
      { return credential.getValue();
      }
    }
    return null;
  }

  @Override
  protected boolean store(T val)
  { return false;
  }

  @Override
  public boolean isWritable()
  { return false;
  }
}
