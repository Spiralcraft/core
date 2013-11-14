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
package spiralcraft.lang.kit;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.TypeModel;
import spiralcraft.lang.kit.members.MetaCastMember;
import spiralcraft.lang.kit.members.MetaChannelMember;
import spiralcraft.lang.kit.members.MetaEmptyMember;
import spiralcraft.lang.kit.members.MetaFocusMember;
import spiralcraft.lang.kit.members.MetaLastMember;
import spiralcraft.lang.kit.members.MetaListMember;
import spiralcraft.lang.kit.members.MetaLogMember;
import spiralcraft.lang.kit.members.MetaNilMember;
import spiralcraft.lang.kit.members.MetaSelfMember;
import spiralcraft.lang.kit.members.MetaSizeMember;
import spiralcraft.lang.kit.members.MetaSubtypeMember;
import spiralcraft.lang.kit.members.MetaTopMember;
import spiralcraft.lang.kit.members.MetaTuneMember;
import spiralcraft.lang.kit.members.MetaTypeMember;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.util.string.StringConverter;

import java.net.URI;
import java.util.HashMap;
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

  private static final HashMap<String,Member<Reflector<?>,?,?>> 
    metaMembers=new HashMap<String,Member<Reflector<?>,?,?>>();
  
  @SuppressWarnings("rawtypes")
  private static void addStandardMetaMembers()
  {
    addMetaMembers
      (new Member[]
        { new MetaTypeMember()
        , new MetaSubtypeMember()
        , new MetaChannelMember()
        , new MetaFocusMember()
        , new MetaCastMember()
        , new MetaNilMember()
        , new MetaTopMember()
        , new MetaLastMember()
        , new MetaLogMember()
        , new MetaTuneMember()
        , new MetaSizeMember()
        , new MetaEmptyMember()
        , new MetaListMember()
        , new MetaSelfMember()
        }
      );

  }

  static {
    addStandardMetaMembers();
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static final void addMetaMembers
    ( Member ... members)
  {
    for (Member<Reflector<?>,?,?> member:members)
    { metaMembers.put(member.getName(),member);
    }
  }
  
  
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
    
    Member member=metaMembers.get(name);
    if (member!=null)
    { channel=member.resolve(this,source,focus,params);
    }
    else if (Reflector.class.isAssignableFrom(source.getContentType()))
    { 
      Reflector<?> reflector=(Reflector<?>) source.get();
      channel=reflector.getEnum(name.substring(1));
      
      if (channel==null)
      {
      
        Channel staticChannel
          =reflector.getStaticChannel(focus);
      
        // Check static channel for fluent syntax
        channel=reflector.<X>resolve
          (staticChannel,focus,name.substring(1),params);
      }
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
  

  
  
  
  /**
   * Override this method to return a custom cast
   * 
   * @param <X>
   * @param source
   * @param targetType
   * @return
   */
//  public <X> Channel<X> newConversionChannel
//    (Channel<T> source,Reflector<X> targetType)
//  { return null;
//  }
  
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
    LinkedList<Signature> ret;
    if (base==null)
    {
      ret=new LinkedList<Signature>();
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
    }
    else
    { ret=base.getSignatures(source);
    }
    return ret;
  }
  
  @Override
  public LinkedList<Signature> getProperties(Channel<?> source)
    throws BindException
  {
    LinkedList<Signature> ret;
    if (base==null)
    { ret=new LinkedList<Signature>();
    }
    else
    { ret=base.getProperties(source);
    }
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
  public Channel<T> createNilChannel()
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
        =LangUtil.constantChannel((Reflector<T>) this);

    }
  }
  
  @Override
  public Channel<T> getStaticChannel(Focus<?> context)
  { 
    Channel<T> staticChannel
      =new SimpleChannel<T>(this,null,true);
    staticChannel.setContext(context);
    
    return staticChannel;
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
  
  @Override
  public Channel<T> getEnum(String name)
  { return null;
  }
  
}













