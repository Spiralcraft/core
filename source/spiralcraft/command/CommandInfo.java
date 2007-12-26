//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.command;

import java.net.URI;

/**
 * <p>Describes a Command for the purpose of programmatic UI generation.
 * </p>
 * 
 * <p>A CommandInfo will usually be contextualized- ie. Strings will be 
 *   internationalized, and an icon image set may be predetermined.
 * </p>
 *
 * @author mike
 */
public interface CommandInfo
{
  /**
   * @return the programmatic id associated with command, referenced in UI
   *   configuration and used to obtain a CommandFactory from a Commandable.
   */
  String getId();
  
  
  /**
   * @return A short internationalized name for use in a menu
   */
  String getName();
  
  
  /**
   * @return A short internationalized description for use in a "Tool Tip"
   *   or brief help text
   */
  String getDescription();

  
  /**
   * @return The base URI of the visual icon for use in a UI. This URI will be
   *   further resolved to obtain a suitable format for the specific use within
   *   the UI.
   *   
   * <p>XXX This convention needs to be further specified
   * </p>
   */
  URI getBaseIconURI();
  
  /**
   * @return An internationalized resource which contains a more complete
   *   description of this command for the end user. 
   */
  URI getHelpTextURI();
  
  
}
