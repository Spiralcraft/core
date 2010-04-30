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


import spiralcraft.common.Lifecycle;
import spiralcraft.lang.FocusChainObject;

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
  extends FocusChainObject,Lifecycle
{
  
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
   * Create a new State subtype which represents this Component's
   *   conversational state.
   * 
   * @param parent
   * @return
   */
  State createState(State parent);

  
  /**
   * Return a reference to the Container aspect of this object, if this
   *   Component supports containership.
   * 
   * @return
   */
  public Container asContainer();
  
  /**
   * Return a reference to the Parent aspect of this object, if this
   *   Component is a Parent.
   * 
   * @return
   */
  public Parent asParent();
  
  
  /**
   * Return a reference to the Context which contains this
   *   Component
   * 
   * @return
   */
  public Parent getParent();
  

  

}
