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
package spiralcraft.command;

/**
 * Encapsulates high level functionality executed on behalf of a user agent
 */
public interface Command
{
  /**
   * Create a new ParameterSet appropriate for this Command
   */
  ParameterSet newParameterSet();
  
  /**
   * Execute the command
   */
  Object execute(CommandContext context,ParameterSet params);
  
  /**
   * Short description of the command (ie. a tool tip), or an ID referencing
   *   such a description.
   */
  String getDescription();

  /**
   * The name of the Command
   */
  String getName();
}
