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

import java.net.URI;
import java.util.LinkedList;

import spiralcraft.common.namespace.PrefixResolver;

/**
 * <p>A starting point for the evaluation of an Expression, which provides
 *   access to a chain of different contexts that can be referenced by the
 *   EL. 
 * </p>
 * 
 * <p>The Focus interface effectively implements a "hierarchically localized
 *   singletons" pattern, which is designed to reduce or eliminate the use
 *   of absolute or "global" references in the dependency resolution process.
 * </p> 
 *   
 * <p>A hierarchy of Focus objects comprises a chain that is used to publish
 *   application interfaces at different points in the application 
 *   containership hierarchy
 *   for access by the EL. The instances of these application interfaces are 
 *   made available to the EL via Channels.
 * </p>
 *
 * <p>Expressions bound to a Focus create new Channels by defining traversals,
 *   transformations and computations bound to the data provided by the source
 *   Channels made available in the Focus.
 * </p> 
 * 
 * <h3>Focus Subject and Context- internal referencing</h3>
 * 
 * <p>A Focus is provided by an Expression container in order to allow 
 *   an Expression to access a specific part of an application. In its simplest
 *   form, a Focus provides access to a single "subject" Channel. This
 *   Channel defines the set of names and operations that may be used in
 *   the Expression (via the Reflector object associated with the Channel). 
 * </p> 
 * 
 * <p>For example, an Expression container sets up a Focus to allow the user
 *   or scripter to access various properties of, say, a hypotherical Event
 *   object. The following Expressions, when bound to the Focus, will provide
 *   new Channels that return the values of various properties of the Event
 *   object. 
 *   
 *   <pre>
 *     name
 *     description
 *     location.state
 *     duration.hours+":"+duration.minutes
 *     time.dayOfWeek+", "+time.monthName+" "+time.dayOfMonth+" "+time.year
 *     coordinator.name</pre>
 *   
 *   The Expression container will make the instance or instances of the
 *     Event object available through the "subject" Channel, and the 
 *     downstream Channels created by the above Expressions will provide access
 *     to the computed properties. 
 * </p>
 *  
 * <b>- Telescoping</b>
 * 
 * <p>In many cases, part of an expression needs to refer to a more localized
 *   scope than the original subject Channel in the Focus provided by the
 *   Expression container. Any parameterized operation referenced in an
 *   Expression may narrow, or "telescope" the Focus available to the 
 *   Expressions provided as parameters. This often occurs when an Expression
 *   needs to reference properties or operations of items contained in
 *   containers.
 * </p>
 *   
 * <p>A telescoped Focus references two Channels- a telescoped subject and a 
 *   "context", which refers to the original subject. 
 *   The context provides access to the 'pinned', or more general scope of 
 *   evaluation in circumstances where the evaluation of an expression
 *   traverses recursively deeper.
 * </p>
 * 
 * <p>In the EL, a leading "." operator before a name refers to the telescoped
 *   subject. A name without a leading "." refers to the context. 
 * </p>
 * 
 * <p>For example, consider 2 classes:
 *   
 *   <pre>
 *   class ShelfFilling
 *   {
 *     Shelf destination;
 *     Item[] items;
 *   }
 *   
 *   class Item
 *   {
 *     int height;
 *   }
 *   </pre>
 *   
 *   The Expression container provides a Focus to evaluate Expressions against
 *   the ShelfFilling object, which represents a hypothetical operation to 
 *   fill shelves with items. The expression
 *   <pre>items[.height <= destination.maxHeight]</pre> should evaluate 
 *   to the set of items where Item.height is less than or equal to the
 *   ShelfFilling.destination,maxHeight. In this case, the brackets implemement
 *   a "selection" and telescope the "subject" to that of the individual Item,
 *   to specify its "height" property, but the context remains the ShelfFilling
 *   object.
 * </p>
 * 
 * 
 * <h3>Focus Naming and Resolution - external referencing</h3>
 * 
 * <p>A URI based mechanism is provided for an expression to access data
 *   external to its immediate context. This allows an Expression container
 *   to expose all or part of its hierarchical containership model and/or 
 *   its layering model to Expressions.
 * </p> 
 *   
 * <p>The "Focus resolution prefix" is prepended to a name to refer to a 
 *   different Focus than the default. The Focus Specifier between the
 *   
 * </p>
 * 
 * <pre>FocusResolutionPrefix = "[" focusSpecifier "]"
 * <pre>FocusSpecifier = ((namespace ":") name) ("?" query) ("#" layerName) "]"</pre>
 * 
 * <p>namespace and name follow the rules of the XML Namespaces W3C
 *   recommendation. The namespace translates to a URI, against which 
 *   the rest of the focusSpecifier is resolved, resulting in a fully
 *   qualified URI. 
 * </p>
 * 
 * <p>query follows the x-form-urlencoded mime type</p>
 * 
 * <p>layerName is a string unique to the layer namespace implementation,
 *   which by convention is the Java package which "implements" the
 *   desired layer namespace.
 * </p>
 * 
 * <p>A namespace mapped to a URI is used to query the Focus chain for a given
 *   interface against which to evaluate an expression. The namespaces are 
 *   mapped by the container which hosts the Focus, and will often mirror
 *   namespace definitions in the medium that contains the expressions, for
 *   example XML namespaces for expressions embedded in XML, or custom
 *   namespaces defined by an application
 * </p>
 * 
 * <p>If a namespace is not specified, a default namespace is usually defined.
 * </p>
 *   
 * <p>When the translated URI refers to more than one interface in the Focus
 *   chain, the "nearest" ancestor in the containership hierarchy is used.
 *   If this is not the desired behavior, the query string can be used to
 *   map attribute names to XXX(finalize) to locate a suitable layer.
 * </p>
 * 
 * <p>Multiple "layers" may be exposed by a Focus. A different layer in a
 *   Focus can be selected using the "#layerName" suffix of the Focus. 
 * 
 * <p>The following examples refer to the "value" property of the object
 *   published in the "default" Focus.
 * 
 *   <pre>value</pre>
 *   <pre>[] value</pre>
 * </p>
 * 
 * <p>The following example refers to a the "title" property of a fictitious 
 *   "Document" object published somewhere in the Focus chain. The expression
 *   below is evaluated in the context of Document, which is exposed as a
 *   singleton via a Focus associated with a particular Document.
 *   
 *   <pre>[myPackage:Document] title</pre>
 *   
 * </p>
 *
 * <p>The following example refers to the "title" property of a fictitious 
 *   "BarContainer" object published somewhere in the Focus chain, where 
 *   BarContainer contains other BarContainers, and one of them has an 
 *   "id" attribute = "foo".
 *   
 *   <pre>[myPackage:BarContainer?id=foo] title</pre>
 *   
 * </p>
 *
 * <p>The following example accesses the 'spiralcraft.data' layer provided by
 *     the "BarDataset" object, exposing its own set of EL bindings, in this
 *    case, a "myList" container of objects with a "myField" property.
 *   
 *   <pre>[myPackage:BarDataset#spiralcraft.data] myList[0].myField</pre>
 *   
 * </p>
 */
public interface Focus<T>
{
    
  /**
   * Return the Context of this Focus (the 'workspace' in which the computation
   *   is being performed). A Focus inherits its parent's Context if it does
   *   not have one of its own.
   */
  Channel<?> getContext();

  /**
   * Return the subject of expression evaluation
   */
  Channel<T> getSubject();
  
  /**
   * <p>Find a Focus in the chain with a subject Channel that provides data of 
   *   the type specified by the URI.
   * </p>
   * 
   * <p>A given Focus implementation may provide a reference to itself or
   *   a different Focus for a given specifier. The specifier is resolved up 
   *   the Focus chain. If this Focus does not respond to the specifier, it
   *   will delegate to its parent if it has one.
   * </p>
   */
  <X> Focus<X> findFocus(URI specifier);  

  /**
   * <p>Indicate whether this Focus responds to the URI specifier, 
   *   disregarding any query or fragment portion.
   * </p>
   * 
   * <p>A Focus responds to a specifier if the data type
   *   (or any supertype/interface) of its subject responds to the specifier
   *   as a type URI, or if its containerURI matches the specifier.
   * 
   * @param typeURI
   * @return
   */
  boolean isFocus(URI specifier);

  /**
   * @return the PrefixResolver that associates namespace prefixes with
   *   URIs
   */
  PrefixResolver getNamespaceResolver();
  
  /**
   * Return this Focus's parent Focus.
   */
  Focus<?> getParentFocus();

  /**
   * Return a Channel, which binds the Expression to this Focus. It may be
   *   useful for implementations to cache Channels to avoid creating
   *   multiple channels for the same Expression.
   */
  <X> Channel<X> bind(Expression<X> expression)
    throws BindException;
  
  /**
   * 
   * @return The chain of Focus objects visible from this Focus.
   */
  public LinkedList<Focus<?>> getFocusChain();
  
  /**
   * <p>Return a new Focus that has the same context as this Focus, but 
   *   that has the specified subject
   * </p>
   * 
   * <p>The context is accessed using non-dotted identifiers (eg. "name"),
   *   whereas the subject is accessed using dotted identifiers (eg. ".name").
   *   This provides the capability for simple in-context comparisions,
   *   (eg. ".name==name")
   * </p>
   * 
   * @param <Tchannel>
   * @param channel
   * @return The new Focus, which is a TeleFocus.
   */
  public <Tchannel> TeleFocus<Tchannel> telescope(Channel<Tchannel> subject);
  
  /**
   * <p>Return a new Focus for the end of the Focus chain that references the
   *   specified channel as its subject and context, and that has this Focus
   *   as its parent.
   * </p>
   * 
   * 
   * @param <Tchannel>
   * @param channel
   * @return The new Focus
   */
  public <Tchannel> Focus<Tchannel> chain(Channel<Tchannel> channel);

  /**
   * <p>Return a new Focus for the end of the Focus chain that adds a set
   *   of namespace prefix definitions.
   * </p>
   * 
   * 
   * @param <Tchannel>
   * @param channel
   * @return The new Focus
   */
  public Focus<T> chain(PrefixResolver resolver);
  
  
  /**
   * <p>A Channel that references the Focus object itself (primarily for
   *   metadata and development purposes)
   * </p>
   * 
   * @return The selfChannel
   */
  public Channel<Focus<T>> getSelfChannel();
  
  /**
   * <p>Makes the provided Focus visible from anything scoped beneath this
   *   Focus in the chain. Allows a Focus to expose multiple subjects.
   * </p>
   * 
   * <p>When resolving a Focus URI, the immediate facets of this Focus will
   *   be checked and returned.
   * </p>
   * 
   * @param focus
   */
  public void addFacet(Focus<?> focus);
  
  /**
   * Add an alias to this Focus which will make it responsive to the provided
   *   URI 
   * 
   * @param uri
   */
  public void addAlias(URI uri);
  
  /**
   * Development aid to represent the Focus and contents as an indented block
   * 
   * @param prefix
   * @return
   */
  public String toFormattedString(String prefix);
  
}
