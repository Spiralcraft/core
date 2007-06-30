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
package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

/**
 * A bidirectional data translation associated with an Channel.
 * 
 * A "derived" Object is obtained from the "origin" object in the "get"
 *   direction, and the "origin" object is obtained from the "derived" object
 *   in the "set" direction.
 */
public interface Translator<Tderived,Torigin>
{
  /**
   * Transform the source object in the 'get' direction
   */
  public Tderived translateForGet(Torigin source,Channel[] modifiers);

  /**
   * Transform the source object in the 'set' direction
   */
  public Torigin translateForSet(Tderived source,Channel[] modifiers);

  /**
   * Return the Reflector associated with the derived Object.
   */
  public Reflector<Tderived> getReflector();
}
