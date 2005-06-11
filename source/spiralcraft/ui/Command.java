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
package spiralcraft.ui;

import java.beans.PropertyChangeListener;

/**
 * Implements the Command design pattern, permitting isolation of UI and
 *   application logic. Applications that wish to expose behavioral control
 *   to a UI should provide it through the Command interface 
 */
public interface Command
{
  /**
   * Return a CommandInfo object which describes this
   *   Command to the UI
   */
  public CommandInfo getInfo();

  /**
   * Execute the command
   */
  public void execute();

  /**
   * Indicate whether the the UI should allow
   *   execution of this command.
   */
  public boolean isEnabled();

  /**
   * Add a listener to be notified when enabled state changes.
   */
  public void addPropertyChangeListener
    (PropertyChangeListener listener);

  public void removePropertyChangeListener
    (PropertyChangeListener listener);
}
