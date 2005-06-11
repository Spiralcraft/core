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
package spiralcraft.registry;

/**
 * Implemented by objects that wish to be referencable
 *   within a global name hierarchy. Registrants are
 *   automatically added to the hierarchy by whichever
 *   container mechanism is responsible for defining 
 *   the component's 'name'.
 */
public interface Registrant
{

  /**
   * Called by containers to supply the Registrant
   *   with its RegistryNode, which the Registrant
   *   can use to obtain references to hierachically
   *   organized application services such as logging,
   *   monitoring and lightweight persistence (ie. preferences)
   */
  public void register(RegistryNode node);

}
