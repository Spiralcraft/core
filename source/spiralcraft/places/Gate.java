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
package spiralcraft.places;

/**
 * A means for a UserAgent to gain access to a Place.
 *
 * A Gate is always associated with a single Place.
 *
 * Multiple UserAgents can use the same Gate, though each UserAgent will
 *   receive its own View. 
 */
public interface Gate
{ 
  /**
   * Open the Gate and obtain a View of the Place appropriate for
   *   the specified UserAgent.
   */
  View open(UserAgent agent);
  
  /**
   * Close the Gate with respect the the specified View
   */
  void close(View view);
}
