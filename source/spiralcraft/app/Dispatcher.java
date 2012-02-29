//
// Copyright (c) 1998,2010 Michael Toth
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

import spiralcraft.util.Sequence;




/**
 * <p>Routes Messages through a stateful Component graph for a thread
 * </p>
 * 
 * @author mike
 */
public interface Dispatcher
{
  
  /**
   * @return The State currently being traversed. 
   */
  State getState();
  
  /**
   * <p>A StateFrame defines a period where a stateful Component may process
   *   a sequence of messages atomically and/or in isolation from changes
   *   to external data.
   * </p>
   * 
   * </p>A change in the StateFrame indicates that a component should
   *   re-synchronize its state with the external environment.
   * </p>
   * 
   * @return The StateFrame associated with the current set of messages.
   */
  StateFrame getFrame();

  /**
   *   
   * <p>A stateful model allows for interactivity, but costs memory and
   *   CPU
   * </p>
   * 
   * @return Whether States should be created and maintained for
   *   components.
   * 
   * 
   * @return Whether state is maintained during message dispatch
   */
  boolean isStateful();
  
  /**
   * @return the index of the next route segment
   *   (State tree branch) to traverse, or null
   *   if the end of the specified route has been
   *   reached.
   */
  Integer getNextRoute();
  
  /**
   * Descend into a child state of the current state. The index must reference
   *   an existing child state. 
   */
  void descend(int index,boolean outOfBand);
  
  /**
   * Ascend to the current state's parent state in the state tree. This method
   *   must always be called once for each successful call to descend().
   */
  void ascend(boolean outOfBand);
  
  /** 
   * Relay a message to a child Component along a specific branch of the 
   *   current state tree.
   * 
   * @param childComponent
   * @param routeSegment
   * @param message
   */
  void relayMessage(Component childComponent,int routeSegment,Message message);
  
  /**
   * <p>Relay a message to a child Component along a specific branch of the
   *   state tree underneath the specified state.
   * </p>
   * 
   * <p>This method permits the calling component to substitute a child state
   *   not included in the route.
   *   
   *   
   * @param childComponent
   * @param newParentState
   * @param routeSegment
   * @param message
   */
  void relayMessage
    (Component childComponent
    ,State newParentState
    ,int routeSegment
    ,Message message
    );
  
  
  /**
   * <p>Send a Message into the Component hierarchy rooted at the 
   *   current Component, routed to the Component at the specified
   *   path.
   * </p>
   * 
   * @param state
   */
  void dispatch
    (Message message
    ,Sequence<Integer> path
    );  
  
  /**
   * <p>Send a Message into the Component hierarchy rooted at the 
   *   specified Component, optionally associated with
   *   the specified State, and routed to the Component at the specified
   *   path.
   * </p>
   * 
   * <p>Called once at the root of the hierarchy to dispatch a message
   * </p>
   * 
   * @param state
   */
  void dispatch
    (Message message
    ,Component component
    ,State state
    ,Sequence<Integer> path
    );
  
  /**
   * Relay an Event up the state tree
   * 
   * @param event
   */
  void handleEvent(Event event);
  
  /**
   * Indicate whether the message is targeted at the current component, i.e.
   *   there no more route segments to process.
   * 
   * @return
   */
  boolean isTarget();
  
  /**
   * Provide information for logs about the context from which the dispatcher
   *   originated.
   * 
   * @return
   */
  String getContextInfo();
}
