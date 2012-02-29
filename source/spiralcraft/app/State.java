//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app;

import spiralcraft.util.Sequence;

/**
 * <p>Represents the state of a Component and its contents in association with 
 *   a specific context, such as a user or a particular source of data.
 * </p>
 *   
 * <h3>Components and State</h3>
 * 
 * <p>The tree of Components that make up a given unit of functionality are
 *   combined with a set of concrete data elements from each data-bound
 *   component to create a concrete data structure represented by a 
 *   tree of States. This State tree represents a "view" of the arbitrary
 *   data underlying a given user or machine interaction context that
 *   has a duration beyond a "functional" method call.  
 * </p>
 *   
 * <p>In the simplest and most memory efficient case, a Component tree may be 
 *   traversed in a stateless fashion, if the end state of the view is entirely
 *   a function of the Component tree and referenced data and no 
 *   "running state" is required.  
 * </p>
 * 
 * <p>In some cases, it is desirable for an Component to keep track of some
 *   stateful information so that data can be completely 
 *   processed before updating any views. This is typically associated with
 *   views that maintain conversational state or transactional state, such as
 *   web or GUI applications. In this scenario, various Messages are passed
 *   to the components in the hierarchy, which update their own states,
 *   the model state, and the view state at various application specific 
 *   times.   
 * </p>
 *   
 * <p>State objects are intended to be lightweight and portable
 *   to permit efficient heap usage and transport. 
 * </p>
 *   
 * <h3>Data structure</h3>
 * 
 * <p>State objects are organized into a tree structure. A State is always 
 *   associated with a Component, and typically references
 *   child States that parallel its Component's children using the integer
 *   indexes within the Component's child list to associate them.
 * </p>
 * 
 * <p>The Dispatcher is responsible for traversing the State tree along
 *   with the Component tree during the process of delivering a Message, and
 *   providing a Component with a reference to its own state.
 * </p>
 * 
 * 
 * @author mike
 *
 */
public interface State
{

  /**
   * 
   * @return The path from the root of the State tree to this state
   */
  Sequence<Integer> getPath();  
  
 
  /**
   * 
   * @return This state's parent state
   */
  State getParent();
  
  /**
   * The n'th child State of this State.
   *
   * @param index
   * @return
   */
  State getChild(int index);
  
 
  /**
   * Link this State into the state tree
   * 
   * @param parentState
   * @param path
   */
  void link(State parentState,Sequence<Integer> path);
  
  /**
   * Replace the n'th child with the specified State tree
   * 
   * @param index
   * @param child
   */
  void setChild(int index,State child);
  
  
  /**
   * Find the first State with the specified type in this States ancestral
   *   Container hierarchy. 
   * 
   * @param <X>
   * @param clazz
   * @return The State with the specific class or interface, or null if
   *   none was found
   */
  <X> X findState(Class<X> clazz);
  
  /**
   * <p>Return an ancestor of this State that is the specified
   *   number of parents away.
   * </p>
   * 
   * @param distance The number of states to traverse, where 0 indicates
   *   that this state should be returned and 1 indicates that this state's
   *   parent should be returned.
   * @return The ancestor state.
   */
  State getAncestor(int distance);
  
  /**
   * The id of the Component that this state belongs to, relative to the
   *   containing component.
   *   
   * @return
   */
  String getComponentId();
  
  /**
   * Called by the dispatcher when a State that is within the target tree
   *   of a message is about to be messaged.
   * 
   * @param frame
   * @return
   */
  void enterFrame(StateFrame frame);
  
  /**
   * Called by the dispatcher when a State that is within the target tree
   *   of a message has completed the processing of a message.
   */
  void exitFrame();
  
  /**
   * Indicates to components that the StateFrame has changed since the 
   *   last time a message was received, and that any state data should be
   *   refreshed.
   * 
   * @return
   */
  boolean isNewFrame();
}


