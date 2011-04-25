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
  void descend(int index);
  
  /**
   * Ascend to the current state's parent state in the state tree. This method
   *   must always be called once for each successful call to descend().
   */
  void ascend();
  
  /**
   * Relay a message to a childComponent along a specific branch of the 
   *   state tree.
   * 
   * @param childComponent
   * @param routeSegment
   * @param message
   */
  void relayMessage(Component childComponent,int routeSegment,Message message);
  
  /**
   * Relay an Event up the state tree
   * 
   * @param event
   */
  void handleEvent(Event event);
}
