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

import java.util.Set;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.lang.Focus;



/**
 * A Container is an interface for managing a group of child components
 * 
 * @author mike
 *
 */
public interface Container
  extends Lifecycle
{
  
  
  /**
   * Bind and register children
   */
  void bind(Focus<?> focus)
    throws ContextualException;
  
  /**
   * The children of this container
   * 
   * @return
   */  
  Component[] getChildren();
  
  /**
   * The child component associated with the specified state index
   * 
   * @param child
   * @return
   */
  Component getChild(int stateIndex);
  
  /**
   * The number of child Components in this Container 
   * 
   * @return
   */
  int getChildCount();
  
  /**
   * Relay a message to the contents of this Container
   * 
   * @param dispatcher
   * @param message
   */
  void relayMessage(Dispatcher dispatcher,Message message);
  
  /**
   * The set of message types that should be propagated down the state tree.
   * 
   * @return
   */
  Set<Message.Type> getSubscribedTypes();
}
