//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.app;


import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;

/**
 * <p>Implements arbitrary functionality within the context of a containership
 *   hierarchy of other Components. 
 * </p>
 * 
 * <p>A Component functions by transforming the context defined by a Parent,
 *   the Focus chain, any thread-context objects, and the set of incoming
 *   Messages into a transformed context available to any child Components.
 * </p>
 * 
 * @author mike
 *
 */
public interface Component
  extends Contextual,Lifecycle
{
  
  /**
   * Bind this Component and everything it contains into the Focus chain.
   * 
   * @param focus
   * @return A Focus that contains anything this component wants to export
   *   to its parent component.
   * @throws ContextualException
   */
  @Override
  Focus<?> bind(Focus<?> focus)
    throws ContextualException;
  
  /**
   * <p>Recursively send a message to one or more descendants, to
   *   provide an opportunity for them to update their state or any of their
   *   bindings.
   * </p>
   * 
   * <p>If a message is intended for a particular Component in the tree, the
   *   first element of the path list will be the index of this components
   *   child to forward the message to. 
   * </p>
   * 
   * <p>Message.isMulticast() determines whether the message is forwarded to
   *   the entire state subtree of the targeted component state, or just the
   *   targeted component state itself.
   * </p>
   * 
   */
  void message
    (Dispatcher context
    ,Message message
    );
  
  /**
   * The message types which will be relayed to the component regardless
   *   of path targeting or multicast status.
   *   
   * @param type
   */
  Message.Type[] getSubscribedTypes();
  
  /**
   * Create a new State which represents this Component's
   *   conversational state.
   * 
   * @param parent
   * @return
   */
  State createState();

  
  /**
   * Return a reference to the Parent aspect of this object, if this
   *   Component is a Parent.
   * 
   * @return
   */
  Parent asParent();
  
  
  /**
   * Return a reference to the Context which contains this
   *   Component
   * 
   * @return
   */
  Parent getParent();
  
  /**
   * Called when a component becomes a child of a Parent.
   * 
   * @param parent
   */
  void setParent(Parent parent);

  /**
   * The volume of log messages emitted from this component
   * 
   * @param debugLevel
   */
  void setLogLevel(Level logLevel);

  /**
   * Uniquely identifies this Component within the set of this Component's
   *   siblings
   *  
   * @return This Component's identifier
   */
  String getId();
}
