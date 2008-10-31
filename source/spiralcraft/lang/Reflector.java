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
package spiralcraft.lang;

import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.util.ArrayUtil;

import java.net.URI;

/**
 * <p>A Reflector is a "type broker" which exposes parts of an object model 
 *   by creating data pipes (Channels) based on elements of Expression syntax
 *   as it applies to the underlying typing model.
 * </p>
 *   
 * <p>Given a data source and a Focus, a Reflector will resolve a name and a set of
 *   modifiers, providing another data source (Channel) bound to the first and to the
 *   Focus, in order to effect some transformation or computation.
 * </p>
 */
public abstract class Reflector<T>
{

  private volatile Channel<Reflector<T>> selfChannel;
  

  /**
   * <p>Generate a new Channel which resolves the meta name (@ prefixed name)
   *  and the given parameter expressions against the source Channel and the
   *  supplied Focus.
   * </p>
   * 
   * <p>Subclasses are free to add their own meta names by overriding this 
   *   method but must call this superclass method to resolve standard names.
   * </p>
   */
  @SuppressWarnings("unchecked")
  public <X> Channel<X> resolveMeta
    (Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  {
    Channel<?> channel=null;
    if (!name.startsWith("@"))
    { return null;
    }
    if (name.equals("@type"))
    { channel=getSelfChannel();
    }
    else if (name.equals("@channel"))
    { 
      channel=source.getCached("@channel");
      if (channel==null)
      { 
        channel=new SimpleChannel(source,true);
        source.cache("@channel",channel);
      }
    }
    else if (name.equals("@focus"))
    { channel=focus.getSelfChannel();
    }
    else if (name.equals("@cast"))
    { 
      if (params.length!=1)
      { 
        throw new BindException
          ("@cast accepts a single parameter: "
          +ArrayUtil.format(params,"|",null)
          );
      }
      Channel<Reflector<X>> typeChannel
        =focus.bind(((Expression<Reflector<X>>) params[0]));
      if (!Reflector.class.isAssignableFrom(typeChannel.getContentType()))
      { throw new BindException("@cast only accepts a type");
      }
      if (!typeChannel.isConstant())
      { throw new BindException("@cast cannot accept a dynamic type");
      }
      Reflector<X> type=typeChannel.get();
      if (type.canCast(this))
      {
        channel=source.getCached(type.getTypeURI());
        if (channel==null)
        { 
          channel=new CastChannel(source,type);
          source.cache(type.getTypeURI(),channel);
        }
      }
      else
      { 
        throw new BindException
          ("Incompatible cast from "
          +getTypeURI()
          +" to "
          +type.getTypeURI()
          );
      }
      
    }
    return (Channel<X>) channel;
  }
  
  /**
   * <p>Indicate whether the source type can be cast to this type
   * </p>
   * 
   * <p>This should be implemented in a permissive manner if not enough
   *   information is available (ie. a runtime determination). This is a
   *   binding time check.
   * </p>
   * 
   * @param source
   * @return
   */
  public boolean canCast(Reflector<?> source)
  { return true;
  }
  
  public Channel<Reflector<T>> getSelfChannel()
  { 
    makeSelfChannel();
    return selfChannel;
  }
  
  private synchronized void makeSelfChannel()
  { 
    if (selfChannel==null)
    {
      try
      {
        selfChannel
          =new SimpleChannel<Reflector<T>>(this,true);
      }
      catch (BindException x)
      { x.printStackTrace();
      }
    }
  }
  
  /**
   * <p>Generate a new Channel which resolves the name and the given parameter 
   *   expressions against the source Channel and the supplied Focus.
   * </p>
   */
  public abstract <X> Channel<X> resolve
    (Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException;

  /**
   * Decorate the specified Channel with a decorator that implements the
   *   specified interface
   */
  public abstract <D extends Decorator<T>> D decorate
    (Channel<T> source,Class<D> decoratorInterface)
    throws BindException;
  
  /**
   * Return the Java class of the data object accessible through Channels 
   *   associated with this Reflector
   */
  public abstract Class<T> getContentType();
  
  /**
   * @return The URI that identifies the specific type of the data objects
   *   described by this Reflector. The URI is defined by the type system
   *   that provides the Reflector implementation.
   */
  public abstract URI getTypeURI();
  
  /**
   * @return Whether the data object described by this Reflector can be
   *   assigned to the type identified by the typeURI. If typeURI==getTypeURI()
   *   then this method must return true. Otherwise, the type compatibility
   *   is defined by the type system that provides the Reflector 
   *   implementation.
   */
  public abstract boolean isAssignableTo(URI typeURI);
  
  /**
   * <p>A Reflector which represents a formally named type is usually associated
   *   with a TypeModel, which supports the retrieval of type Reflectors from
   *   their URIs.
   * </p>
   * 
   * <p>Some Reflectors which represent "anonymous" types-
   *   ie. arbitrary collections of members- and may not have a formal naming
   *   system and or an associated TypeModel.
   * </p>
   *   
   * 
   * @return The TypeModel, if any, to which this Reflector belongs, or null
   *   if this Reflector is not associated with a TypeModel. 
   */
  public TypeModel getTypeModel()
  { return null;
  }
  
  /**
   * <p>Perform a runtime check to see if this value is compatible with this
   *   type. This may be expensive.
   * </p>
   * 
   * @param val
   * @return true, if the value is compatible
   */
  public boolean accepts(Object val)
  { 
    return true;
  }
  
}

class CastChannel<S,T extends S>
  extends AbstractChannel<T>
{
  private Channel<S> source;
  
  public CastChannel(Channel<S> source,Reflector<T> type)
  { 
    super(type);
    this.source=source;
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
}
