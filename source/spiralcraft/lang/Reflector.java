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
import spiralcraft.util.string.StringConverter;

import java.net.URI;
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
public interface Reflector<T>
{

  
  
  /**
   * 
   * @return The set of member Signatures published by this reflector. If
   *   null, the set of Signatures is not obtainable.
   */
  public LinkedList<Signature> getSignatures(Channel<?> source)
    throws BindException;
  
  /**
   * Determine the most specific type of a value
   * 
   * @param val 
   */
  public Reflector<T> subtype(T val);
  
  /**
   * @return A Channel<T> connected to nothing, useful for type
   *   inference.
   */
  public Channel<T> getNilChannel();
  
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
  public boolean canCastFrom(Reflector<?> source);
  
  /**
   * A Channel that can be used to examine this Reflector
   * 
   * @return
   */
  public Channel<Reflector<T>> getSelfChannel();
  
  
  /**
   * <p>Generate a new Channel which resolves the name and the given parameter 
   *   expressions against the source Channel and the supplied Focus.
   * </p>
   */
  public <X> Channel<X> resolve
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
  public <D extends Decorator<T>> D decorate
    (Channel<T> source,Class<D> decoratorInterface)
    throws BindException;
  
  /**
   * Return the Java class of the data object accessible through Channels 
   *   associated with this Reflector
   */
  public Class<T> getContentType();
  
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
   * 
   * @param reflector
   * @return Whether the type represented by this reflector can be
   *   assigned a value of a type represented by the specified reflector.
   */
  public boolean isAssignableFrom(Reflector<?> reflector);
  
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
  public TypeModel getTypeModel();
  
  /**
   * <p>Return the more responsive Reflector of two reflectors that have the
   *   same type URI. Permits type models which utilize others to avoid hiding
   *   underlying types. Returns Reflector.this by default.
   * </p>
   * 
   * @param alternate
   * @return
   */
  public Reflector<?> disambiguate(Reflector<?> alternate);
  
  /**
   * <p>Return the StringConverter most applicable to the type being exposed
   *   by this Reflector, which implements the canonical String encoding
   *   for values of this type.
   * </p>
   * @return
   */
  public StringConverter<T> getStringConverter();
  
  /**
   * Return the common supertype that this Reflector shares with another
   *   reflector 
   * 
   * @param other
   * @return
   * @throws BindException
   */
  public Reflector<?> getCommonType(Reflector<?> other)
    throws BindException;
  
  /**
   * <p>Perform a runtime check to see if this value is compatible with this
   *   type. This may be expensive, but is required for a cast to return null
   *   if the type is not compatible.
   * </p>
   * 
   * @param val
   * @return true, if the value is compatible with this type
   */
  public boolean accepts(Object val);
  
  /**
   * <p>Indicate whether the referenced object is a callable Functor.
   * </p>
   * 
   * @return
   */
  public boolean isFunctor();
  
}
