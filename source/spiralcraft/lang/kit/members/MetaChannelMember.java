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
import spiralcraft.lang.util.LangUtil;

/**
 * Returns a reference to the Channel object that implements the subject
 *   reference
 */
public class MetaChannelMember<T>
  extends Member<Reflector<T>,Channel<T>,T>
{


  { name="@channel";
  }
  
  @Override
  public Channel<Channel<T>> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { 
    assertNoArguments(arguments);
    Channel<Channel<T>> channel=source.getCached(name);
    if (channel==null)
    { 
      channel
        =LangUtil.constantChannel(source);
      source.cache(name,channel);
    }
    return channel;
  }

}
