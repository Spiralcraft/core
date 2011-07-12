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

import java.beans.PropertyChangeSupport;

import java.net.URI;


/**
 * <P>A Channel is a "data pipe" that provides a "view" of the contents of an 
 *   arbitrary data source or container, the "model-part", that exists within
 *   an arbitrary object model.<P>
 *
 * <P>A Channel is able to track changes to the model-part over time, and in
 *   some cases may update the contents of the model-part.<P>
 *
 * <P>As a "view", the Channel is transformative- it provides a specific
 *   representation of the model-part contents which may consist of a subset
 *   or superset of the information contained within the model-part.<P>
 *
 * <P>Channels are combined using an expression language. The expression
 *   language defines a tree of operations to perform against a set of names
 *   available from a name resolution context called a Focus. 
 *
 * <P>A Channel provides a get() and set() method as its primary interface.
 *   These methods accept objects of a predefined type, expressed by 
 *   getContentType(), which returns a Java class.  
 * 
 * <P>A Channel provides another layer of type information in the form of
 *   a Reflector returned from the getReflector() method. The Reflector defines
 *   how language elements are transformed into data access and computation,
 *   and provides an implementation in the form of a new Channel which
 *   references the first, and is returned via the first Channel's resolve()
 *   method.
 *   
 * <P>The resolution of a new Channel from an existing Channel is a named,
 *   parameterized operation. In many cases, ie. simple 'properties',
 *   a single name creates a new Channel which returns the 'property value'
 *   associated with the name by the Reflector.
 *   
 * <P>In other cases, parameters are provided as expressions. The expressions
 *   are bound to a Focus to provide Channels which deliver parameter values.
 *   The Focus against which the parameter expressions are evaluated can either
 *   be supplied externally, or the Reflector may decide to create its own.
 *   
 * <P>For example, the expression <CODE>foo[.name==bar.name]</CODE> is intended
 *   to return the subset of the indexable object 'foo' where the 'name' 
 *   property of an indexed element equals the 'name' property of the object
 *   named 'bar' in the externally supplied context.
 *   
 * <P>A Channel may be 'decorated' with interfaces that provide support for 
 *   certain operations, for example, Iteration. The decorate() method will
 *   resolve an appropriate implementation of the desired interface (again, via
 *   the Reflector) and bind it to the Channel.
 * 
 * <P>The actual decorator determines the specifics of its own use. As channels
 *   do not create their own state, a Decorator will usually provide some 
 *   convenient means to manage a stateful operation. 
 *
 */
public interface Channel<T>
{
  /**
   * Resolve the name and optional set of parameter expressions to create
   *   a new view (Channel) derived from this one.
   *   
   * @param parameters The expressions to use as parameters, or null for a
   *   non parameterized name. Null is differentiated from an empty array.
   */
  <X> Channel<X> resolve
    (Focus<?> focus
    ,String name
    ,Expression<?>[] parameters
    )
    throws BindException;


  /**
   * Resolve contextual metadata for this Channel from an appropriate provider
   *   in the Focus chain.
   * 
   * @param <X>
   * @param focus
   * @param metadataTypeURI
   * @return
   * @throws BindException 
   */
  <X> Channel<X> resolveMeta(Focus<?> focus,URI metadataTypeURI) 
    throws BindException;
  
  /**
   * Return the content of this view.
   */
  T get()
    throws AccessException;

  /**
   * Update the content this view, if the transformation associated with
   *   this Channel and its data sources is reversible.
   *
   *@return Whether the modification was successful or not. A value of false
   *  indicates that the modification request was ignored. If isWritable()
   *  returns false, this method should return false.
   *@throws AccessException if the target of the write throws an exception
   */
  boolean set(T value)
    throws AccessException;

  /**
   * 
   * @return Whether this Channel is writeable- ie. whether set() can be
   *   attempted.
   *   
   * @throws AccessException
   */
  boolean isWritable()
    throws AccessException;

  /**
   * Indicate the Java Class of the content of this View.
   */
  Class<T> getContentType();

  /**
   * <P>Decorate this Channel with a suitable implementation of the
   *   specified decoratorInterface for type of data this Channel accesses.
   *   
   *@return The decorator that implements the decoratorInterface, or null if
   *  the decoratorInterface is not supported
   */
  <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
    throws BindException;
 
  /**
   * Provide a reference to the PropertyChangeSupport object
   *   which fires a PropertyChangeEvent when the data source for this view
   *   changes. Returns null if the this view or any of its data sources do
   *   not support property change notification, or if the content is guaranteed
   *   to remain unchanged.
   */
  PropertyChangeSupport propertyChangeSupport();

  /** 
   * Indicates whether the referenced data value is guaranteed to
   *   remain unchanged.
   */
  boolean isConstant();
  
  /**
   * The spiralcraft.lang.Reflector extended type of the referenced data 
   */
  Reflector<T> getReflector();
  
  /**
   * Return a Channel cached by cache().
   *  
   * @param key
   * @return
   */
  <X> Channel<X> getCached(Object key);
  
  /**
   * The Focus in scope when this Channel was bound. 
   * 
   * @return
   */  
  void setContext(Focus<?> context);

  /**
   * The Focus in scope when this Channel was bound. 
   * 
   * @return
   */
  Focus<?> getContext();
  
  
  /**
   * Cache a Channel that is sourced from this Channel. to avoid creating 
   *   redundant channel instances. A Channel should only be cached in this
   *   manner if it depends solely on this Channel for input.
   *  
   * @param key
   * @return
   */
  void cache(Object key,Channel<?> channel);
  
  /**
   * Provide a data flow trace to the spiralcraft.log.ClassLogger log.
   */
  void setDebug(boolean debug);
}
