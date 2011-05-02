//
// Copyright (c) 1998,2011 Michael Toth
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
package spiralcraft.lang.kit.members;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.Member;

/**
 * Returns a nil reference typed according to the source, which must be
 *   a Reflector (meta) type.
 */
public class MetaNilMember<T>
  extends Member<Reflector<Reflector<T>>,T,Reflector<T>>
{


  { name="@nil";
  }
  
  @Override
  public Channel<T> resolve(
    Reflector<Reflector<T>> reflector,
    Channel<Reflector<T>> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {     
    assertNoArguments(arguments);
    
    Channel<T> channel=source.get().createNilChannel();
    channel.setContext(focus);
    return channel;
  }
  

}
