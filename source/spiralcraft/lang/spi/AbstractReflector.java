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
package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.TypeModel;
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
public abstract class AbstractReflector<T>
  implements Reflector<T>
{

  
  private volatile Channel<Reflector<T>> selfChannel;
  protected final Reflector<T> base;
  protected boolean functor;

  protected AbstractReflector()
  { base=null;
  }

  protected AbstractReflector(Reflector<T> base)
  { this.base=base;
  }
  
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
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
    { 
      channel=((Channel<Reflector<?>>) source).get().getNilChannel();
      channel.setContext(focus);
    }
    else if (name.equals("@top") && params==null)
    { 
      channel=source.getCached("@top");
      if (channel==null)
      { 
        ListDecorator d
          =source.<ListDecorator>decorate(ListDecorator.class);
        if (d!=null)
        { 
          channel=new TopListChannel(source,d);
          source.cache("@top",channel);
        }
      }
      if (channel==null)
      { 
        IterationDecorator d
          =source.<IterationDecorator>decorate(IterationDecorator.class);
        if (d!=null)
        { 
          channel=new TopIterChannel(source,d);
          source.cache("@top",channel);
        }
      }
    }
    else if (name.equals("@last") && params==null)
    { 
      channel=source.getCached("@last");
      if (channel==null)
      { 
        ListDecorator d
          =source.<ListDecorator>decorate(ListDecorator.class);
        if (d!=null)
        { 
          channel=new LastListChannel(source,d);
          source.cache("@last",channel);
        }
      }
      if (channel==null)
      { 
        IterationDecorator d
          =source.<IterationDecorator>decorate(IterationDecorator.class);
        if (d!=null)
        { 
          channel=new LastIterChannel(source,d);
          source.cache("@last",channel);
        }
      }
    }    
    else if (name.equals("@log") && params.length==1)
    { 
      assertSingleParameter(params,name,"String");
      channel=new LogChannel
        (source,focus,params[0]);
    }
    else if (name.equals("@tune") && params.length==1)
    { 
      assertSingleParameter(params,name,"String");
      channel=new TuneChannel
        (source,focus,params[0]);
    }
    else if (name.equals("@size"))
    {
      assertNoParameters(params,name);
      channel=new CollectionSizeChannel(source);
    }
    return (Channel<X>) channel;
  }
  
  protected void assertSingleParameter(Expression<?>[] params,String name,String type)
    throws BindException
  { 
    if (params==null || params.length!=1)
    { throw new BindException(name+" accepts a single parameter of type "+type);
    }
  }
  
  protected void assertNoParameters(Expression<?>[] params,String name)
    throws BindException
  { 
    if (params!=null && params.length>0)
    { throw new BindException(name+" does not accept parameters");
    }
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
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
  @Override
  public Reflector<T> subtype(T val)
  { 
    throw new AccessException
      ("Subtype not supported for type system "+getClass().getName());
  }
  
  /**
   * @return A Channel<T> connected to nothing, useful for type
   *   inference.
   */
  @Override
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
  @Override
  public boolean canCastFrom(Reflector<?> source)
  { 
    if (base!=null)
    { return base.canCastFrom(source);
    }    
    return true;
  }
  
  @Override
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
  @Override
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
  @Override
  public abstract <D extends Decorator<T>> D decorate
    (Channel<T> source,Class<D> decoratorInterface)
    throws BindException;
  
  /**
   * Return the Java class of the data object accessible through Channels 
   *   associated with this Reflector
   */
  @Override
  public abstract Class<T> getContentType();
  
  /**
   * @return The URI that identifies the specific type of the data objects
   *   described by this Reflector. The URI is defined by the type system
   *   that provides the Reflector implementation.
   */
  @Override
  public abstract URI getTypeURI();
  
  /**
   * @return Whether the data object described by this Reflector can be
   *   assigned to the type identified by the typeURI. If typeURI==getTypeURI()
   *   then this method must return true. Otherwise, the type compatibility
   *   is defined by the type system that provides the Reflector 
   *   implementation.
   */
  @Override
  public abstract boolean isAssignableTo(URI typeURI);
  
  @Override
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
  @Override
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
  @Override
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
  @Override
  public StringConverter<T> getStringConverter()
  { return StringConverter.getInstance(getContentType());
  }
  
  @Override
  public Reflector<?> getCommonType(Reflector<?> other)
  {
    if (other==this)
    { return this;
    }
    if (base!=null)
    { return base.getCommonType(other);
    }
    
    Reflector<?> reflector=null;
    if (getContentType()==Void.class || getContentType()==Void.TYPE)
    { reflector=other;
    }
    else if (other.getContentType()==Void.class 
              || other.getContentType()==Void.TYPE
            )
    { reflector=this;
    }
    else if (isAssignableFrom(other))
    { reflector=this;
    }
    else if (other.isAssignableFrom(this))
    { reflector=other;
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
  @Override
  public boolean accepts(Object val)
  { 
    if (base!=null)
    { return base.accepts(val);
    }    
    return true;
  }

  @Override
  public boolean isFunctor()
  { return functor || (base!=null && base.isFunctor());
  }
  
  class SubtypeChannel
    extends SourcedChannel<T,Reflector<T>>
  {

    
    public SubtypeChannel(Channel<T> source)
    { super(getSelfChannel().getReflector(),source);
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
  
  @Override
  public String toString()
  { return super.toString()+":"+getTypeURI();
  }
  
}

class TopListChannel<T,I>
  extends SourcedChannel<T,I>
{
  private final ListDecorator<T,I> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public TopListChannel(Channel<T> source,ListDecorator decorator)
  throws BindException
  { 
    super
    (decorator.getComponentReflector()
    ,source
    );
    this.decorator=decorator;
  }

  @Override
  public I retrieve()
  { 
    T list=source.get();
    if (list!=null)
    { return decorator.get(list,0);
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

class TopIterChannel<T,I>
  extends SourcedChannel<T,I>
{
  private final IterationDecorator<T,I> decorator;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public TopIterChannel(Channel<T> source,IterationDecorator decorator)
    throws BindException
  { 
    super
      (decorator.getComponentReflector()
      ,source
      );
    this.decorator=decorator;
  }
  
  @Override
  public I retrieve()
  { 
    Iterator<I> it=decorator.iterator();
    if (it!=null)
    {
      if (it.hasNext())
      { return it.next();
      }
      else
      { return null;
      }
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

class LastListChannel<T,I>
  extends SourcedChannel<T,I>
{
  private final ListDecorator<T,I> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public LastListChannel(Channel<T> source,ListDecorator decorator)
  throws BindException
  { 
    super
    (decorator.getComponentReflector()
      ,source
    );
    this.decorator=decorator;
  }

  @Override
  public I retrieve()
  { 
    T list=source.get();
    if (list!=null)
    { return decorator.get(list,decorator.size(list)-1);
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

class LastIterChannel<T,I>
  extends SourcedChannel<T,I>
{
  private final IterationDecorator<T,I> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public LastIterChannel(Channel<T> source,IterationDecorator decorator)
  throws BindException
  { 
    super
    (decorator.getComponentReflector()
      ,source
    );
    this.decorator=decorator;
  }

  @Override
  public I retrieve()
  { 
    Iterator<I> it=decorator.iterator();
    if (it!=null)
    {
      I next=null;
      while (it.hasNext())
      { next=it.next();
      }
      return next;
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


class CollectionSizeChannel<T>
  extends SourcedChannel<T,Integer>
{
  private final CollectionDecorator<T,?> decorator;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public CollectionSizeChannel(Channel<T> source)
    throws BindException
  {
    super(BeanReflector.<Integer>getInstance(Integer.class),source);
    this.decorator
      =source.<CollectionDecorator>decorate(CollectionDecorator.class);
    if (decorator==null)
    { 
      throw new BindException
        (source.getReflector().getTypeURI()+" does not support @size()");
    }
  }
  
  @Override
  public Integer retrieve()
  { 
    T collection=source.get();
    if (collection==null)
    { return null;
    }
    else
    { return decorator.size(collection);
    }
  }
  
  @Override
  public boolean store(Integer val)
  { return false;
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
  

}
