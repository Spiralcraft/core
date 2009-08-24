//
// Copyright (c) 1998,2009 Michael Toth
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
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.LogChannel;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.TuneChannel;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.string.StringConverter;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;

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
 * 
 * <p>Reflectors support meta-operations on their associated Channels.
 * </p>
 * 
 * <ul>
 *   <li><b>@type</b>
 *     <p>Binds to this Reflector
 *     </p>
 *   </li>
 *   <li><b>@subtype</b>
 *     <p>Dynamically provides the type of the value in the Channel  
 *     </p>
 *   </li>
 *   <li><b>@channel</b>
 *     <p>Binds to the Channel object itself
 *     </p>
 *   </li>
 *   <li><b>@focus</b>
 *     <p>Binds to the Focus object itself
 *     </p> 
 *   </li>
 *   <li><b>@cast(&lt;typeFocus&gt;)</b>
 *     <p>A new Channel which provides data that is a subtype of
 *       the source
 *     </p>
 *   </li>
 *   <li><b>@nil</b>
 *     <p>A Channel which returns nothing and discards any data applied.
 *       Useful as a source of type inference when storage is not needed.
 *     </p> 
 *   </li>
 * </ul>
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
    else if (name.equals("@subtype"))
    { 
      channel=source.getCached("@subtype");
      if (channel==null)
      { 
        channel=new SubtypeChannel(source);
        source.cache("@subtype",channel);
      }
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
      channel=this.castChannel
        (source,focus,(Expression) params[0]);
      
      
    }
    else if (name.equals("@nil"))
    { channel=((Channel<Reflector<?>>) source).get().getNilChannel();
    }
    else if (name.equals("@top") && params==null)
    { 
      channel=source.getCached("@top");
      if (channel==null)
      { 
        if (source.<IterationDecorator>decorate(IterationDecorator.class)!=null)
        { 
          channel=new TopChannel(source);
          source.cache("@top",channel);
        }
      }
    }
    else if (name.equals("@log") && params.length==1)
    { 
      channel=new LogChannel
        (source,focus,params[0]);
    }
    else if (name.equals("@tune") && params.length==1)
    { 
      channel=new TuneChannel
        (source,focus,params[0]);
    }
    return (Channel<X>) channel;
  }
  
  private <X extends T> Channel<X> castChannel
    (Channel<T> source,Focus<?> focus,Expression<Reflector<X>> target)
      throws BindException
  {
      Channel<X> channel=null;
      Channel<Reflector<X>> targetTypeChannel
        =focus.bind(target);
      
      if (!Reflector.class.isAssignableFrom(targetTypeChannel.getContentType()))
      { throw new BindException("@cast only accepts a type");
      }
      if (!targetTypeChannel.isConstant())
      { throw new BindException("@cast cannot accept a dynamic type");
      }
      Reflector<X> targetType=targetTypeChannel.get();
      if (targetType.canCastFrom(this))
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
        channel=newConversionChannel(source,targetType);
        if (channel==null)
        {
          throw new BindException
            ("Incompatible cast from "
            +getTypeURI()
            +" to "
            +targetType.getTypeURI()
            +" and no default conversion"
            );
        }
      }
      return channel;
    
  }
  
  
  
  /**
   * Override this method to return a custom cast
   * 
   * @param <X>
   * @param source
   * @param targetType
   * @return
   */
  protected <X> Channel<X> newConversionChannel
    (Channel<T> source,Reflector<X> targetType)
  { return null;
  }
  
  /**
   * 
   * @return The set of member Signatures published by this reflector. If
   *   null, the set of Signatures is not obtainable.
   */
  @SuppressWarnings("unchecked")
  public LinkedList<Signature> getSignatures(Channel<?> source)
    throws BindException
  { 
    LinkedList<Signature> ret=new LinkedList<Signature>();
    ret.addFirst
      (new Signature("@type",BeanReflector.getInstance(getClass())));
    ret.addFirst
      (new Signature("@subtype",BeanReflector.getInstance(source.getReflector().getClass())));
    ret.addFirst
      (new Signature("@channel",BeanReflector.getInstance(source.getClass())));
    ret.addFirst
      (new Signature("@focus",BeanReflector.getInstance(Focus.class)));
    ret.addFirst
      (new Signature("@cast",source.getReflector(),BeanReflector.getInstance(Reflector.class)));
    ret.addFirst
      (new Signature("@nil",this));
    
    IterationDecorator iter
      =source.<IterationDecorator>decorate(IterationDecorator.class);
    if (iter!=null)
    { ret.addFirst(new Signature("@top",iter.getComponentReflector()));
    }
    
    ret.addFirst
      (new Signature("@log",BeanReflector.getInstance(Object.class)));
    ret.addFirst
      (new Signature("@tune",BeanReflector.getInstance(Object.class)));
    return ret;
  }
  
  /**
   * @param val
   */
  public Reflector<T> subtype(T val)
  { 
    throw new AccessException
      ("Subtype not supported for type system "+getClass().getName());
  }
  
  /**
   * @return A Channel<T> connected to nothing, useful for type
   *   inference.
   */
  public Channel<T> getNilChannel()
  { 
    return new AbstractChannel<T>(this)
    {

      @Override
      protected T retrieve()
      { return null;
      }

      @Override
      protected boolean store(T val)
        throws AccessException
      { return true;
      }
      
      @Override
      public boolean isWritable()
      { return true;
      }
    };
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
  public boolean canCastFrom(Reflector<?> source)
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
      selfChannel
        =new SimpleChannel<Reflector<T>>(this,true);

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
  
  public boolean isAssignableFrom(Reflector<?> reflector)
  { return reflector.isAssignableTo(getTypeURI());
  }
  
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
   * <p>Return the more responsive Reflector of two reflectors that have the
   *   same type URI. Permits type models which utilize others to avoid hiding
   *   underlying types. Returns Reflector.this by default.
   * </p>
   * 
   * @param alternate
   * @return
   */
  public Reflector<?> disambiguate(Reflector<?> alternate)
  { return this;
  }
  
  /**
   * <p>Return the StringConverter most applicable to the type being exposed
   *   by this Reflector, which implements the canonical String encoding
   *   for values of this type.
   * </p>
   * @return
   */
  public StringConverter<T> getStringConverter()
  { return StringConverter.getInstance(getContentType());
  }
  
  public Reflector<?> getCommonType(Reflector<T> other)
    throws BindException
  {
    Reflector<?> reflector;
    if (getContentType()==Void.class)
    { reflector=other;
    }
    else if (other.getContentType()==Void.class)
    { reflector=this;
    }
    else if (isAssignableFrom(other))
    { reflector=this;
    }
    else if (other.isAssignableFrom(this))
    { reflector=other;
    }
    else
    { 
      throw new BindException
        (getTypeURI()+" has no common type with "+other.getTypeURI());
    }
    return reflector;
  }
  
  /**
   * <p>Perform a runtime check to see if this value is compatible with this
   *   type. This may be expensive, but is required for a cast to return null
   *   if the type is not compatible.
   * </p>
   * 
   * @param val
   * @return true, if the value is compatible with this type
   */
  public boolean accepts(Object val)
  { 
    return true;
  }
  
  class SubtypeChannel
    extends AbstractChannel<Reflector<T>>
  {

    private Channel<T> source;
    
    public SubtypeChannel(Channel<T> source)
    { 
      super(getSelfChannel().getReflector());
      this.source=source;
    }
    @Override
    protected Reflector<T> retrieve()
    { return subtype(source.get());
    }

    @Override
    protected boolean store(
      Reflector<T> val)
      throws AccessException
    { return false;
    }
  }
  
}

class TopChannel<T,I>
  extends AbstractChannel<I>
{
  private final IterationDecorator<T,I> decorator;
  
  @SuppressWarnings("unchecked")
  public TopChannel(Channel<T> source)
    throws BindException
  { 
    super
      (source.<IterationDecorator>decorate(IterationDecorator.class)
      .getComponentReflector()
      );
    decorator=source.<IterationDecorator>decorate(IterationDecorator.class);
    
  }
  
  @Override
  public I retrieve()
  { 
    Iterator<I> it=decorator.iterator();
    if (it.hasNext())
    { return it.next();
    }
    else
    { return null;
    }
  }
  
  @Override
  public boolean store(I val)
  { return false;
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
