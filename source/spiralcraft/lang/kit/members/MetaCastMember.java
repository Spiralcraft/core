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
import spiralcraft.util.refpool.URIPool;

/**
 * Returns 
 */
public class MetaCastMember<T,X extends T>
  extends Member<Reflector<T>,X,T>
{


  { name="@cast";
  }
  
  @Override
  public Channel<X> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { 
    assertRequiresSingleArgument
      (arguments,URIPool.create("class:/spiralcraft/data/Type"));
    
    Channel<X> channel=null;
    @SuppressWarnings("unchecked")
    Expression<Reflector<X>> target = (Expression<Reflector<X>>) arguments[0];
    
    Channel<Reflector<X>> targetTypeChannel
      =focus.bind(target);
    
    if (!Reflector.class.isAssignableFrom(targetTypeChannel.getContentType()))
    { throw new BindException("@cast only accepts a type");
    }
    if (!targetTypeChannel.isConstant())
    { throw new BindException("@cast cannot accept a dynamic type");
    }
    Reflector<X> targetType=targetTypeChannel.get();
    if (targetType.canCastFrom(reflector))
    {
      channel=source.getCached(targetType.getTypeURI());
      if (channel==null)
      { 
        channel=new CastChannel<T,X>(source,targetType);
        source.cache(targetType.getTypeURI(),channel);
      }
    }
    else
    { 
      // channel=newConversionChannel(source,targetType);
      if (channel==null)
      {
        throw new BindException
          ("Incompatible cast from "
          +reflector.getTypeURI()
          +" to "
          +targetType.getTypeURI()
          +" and no default conversion"
          );
      }
    }
    return channel;
    
  }

}


class CastChannel<S,T extends S>
  extends SourcedChannel<S,T>
{
  
  public CastChannel(Channel<S> source,Reflector<T> type)
  { super(type,source);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  { 
    S val=source.get();
    if (getReflector().accepts(val))
    { return (T) val;
    }
    else
    { return null;
    }
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { return source.set(val);
  }

  @Override
  public boolean isWritable()
    throws AccessException
  { return source.isWritable();
  }
  

}
