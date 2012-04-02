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

/**
 * A Parent provides descendants access to ancestors in the Component tree 
 * 
 * @author mike
 *
 */
public interface Parent
{

  /**
   * Pass an event up the component hierarchy
   * 
   * @param dispatcher
   * @param event
   */
  void handleEvent(Dispatcher dispatcher,Event event);
  
  /**
   * <p>Indicate that this parent should subscribe to and forward messages
   *   of the specified types to descendants.
   * </p>
   *   
   * @param subscription
   */
  void subscribe(Message.Type[] types);
  
  /**
   * <p>Find the distance from the calling descendant Component's immediate 
   *   Parent in the state tree to the state of the ancestral Parent of the
   *   specified class.
   * </p>
   * 
   * <p>This provides an efficient means for States to associate with ancestors
   * </p>
   * 
   * 
   * @return The state distance, where 1 indicates an immediate parent and 0
   *   indicates that this Container matches the requested Class.
   */
  int getStateDistance(Class<?> clazz);
  
  /**
   * 
   * @return The Component aspect of this context, for ascending the
   *   tree.
   */
  Component asComponent();

  
  /**
   * <p>Indicates how many levels of States in the State tree are represented
   *   by this Context.
   * </p>
   * 
   * <p>The value is typically 1 for most Contexts, and
   *   2 for Contexts that use an intermediate state, for instance to manage 
   *   multiple states for each child Component.
   * </p>
   *
   * <p>A value of 0 is theoretically possible if the Context does not
   *   use any State and has a single or no child, but is not currently
   *   supported.
   * </p>
   * 
   * @return The state depth of this Component.
   */
  int getStateDepth();  
  
  /**
   * Find a Component among this Component's ancestors/containers
   * 
   * @param <X>
   * @param clazz
   * @return The Element with the specific class or interface, or null if
   *   none was found
   */
  <X> X findComponent(Class<X> clazz);
  
  /**
   * Find a Component among this Component's ancestors/containers, but stop
   *   looking when we reach the stop class.
   * 
   * @param <X>
   * @param clazz
   * @return The Element with the specific class or interface, or null if
   *   none was found
   */
  <X> X findComponent(Class<X> clazz,Class<?> stop);

}
