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


public interface Component
  extends FocusChainObject,Lifecycle
{
  
  /**
   * <p>Recursively send a message to one or more components in the tree, to
   *   provide an opportunity for them to update their state or any of their
   *   bindings.
   * </p>
   * 
   * <p>If a message is intended for a particular Element in the tree, the
   *   first element of the path list will be the index of this element's
   *   child to forward the message to. 
   * </p>
   * 
   * <p>Message.isMulticast() determines whether the message is forwarded to
   *   the entire subtree under the targeted element, as opposed to just 
   *   the targeted element. 
   * </p>
   * 
   * <p>The default behavior is to propagate the message to the appropriate
   *  children. Subclasses which override this method should call 
   *  this superclass method if an event should be propagated.
   * </p>
   * 
   */
  void message
    (MessageContext context
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
   * Return a reference to the Container which directly contains this
   *   Component
   * 
   * @return
   */
  public Container getParent();
  
  /**
   * <p>Find the distance from the calling Component's state in the state
   *   tree to the state of the ancestral Container of the specified class.
   * </p>
   * 
   * <p>This provides an efficient means for States to associate with ancestors
   * </p>
   * 
   * @param clazz The class of the Component being searched for.
   * 
   * @return The state distance, where 1 indicates an immediate parent and 0
   *   indicates that this Element matches the requested Class.
   */
  public int getStateDistance(Class<?> clazz);
  
  /**
   * <p>Indicates how many levels of States in the State tree are represented
   *   by this component.
   * </p>
   * 
   * <p>The value is typically 1 for most Components, and
   *   2 for Components that use an intermediate state, for instance to manage 
   *   multiple states for each child Component.
   * </p>
   *
   * <p>A value of 0 is theoretically possible if the Component does not
   *   use any State and is the only child of its parent. This is not
   *   yet supported, however.
   * </p>
   * 
   * @return The state depth of this Component.
   */
  public int getStateDepth();
}
