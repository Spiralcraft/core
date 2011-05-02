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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.Member;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * Returns a subtype (dynamic instance type) reflector
 */
public class MetaSubtypeMember<T>
  extends Member<Reflector<T>,Reflector<T>,T>
{


  { name="@subtype";
  }
  
  @Override
  public Channel<Reflector<T>> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { 
    assertNoArguments(arguments);
    
    Channel<Reflector<T>> channel=source.getCached(name);
    if (channel==null)
    { 
      channel=new SubtypeChannel<T>(reflector,source);
      source.cache(name,channel);
    }
    return channel;
  }
}

class SubtypeChannel<T>
  extends SourcedChannel<T,Reflector<T>>
{

  private Reflector<T> reflector;
  
  public SubtypeChannel(Reflector<T> reflector,Channel<T> source)
  {
    // Always returns a reflector of the same type model (Reflector class)
    super(reflector.getSelfChannel().getReflector(),source);
    this.reflector=reflector;
  }
  
  @Override
  protected Reflector<T> retrieve()
  { return reflector.subtype(source.get());
  }

  @Override
  protected boolean store(
    Reflector<T> val)
    throws AccessException
  { return false;
  }
}

