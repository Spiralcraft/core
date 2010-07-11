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
package spiralcraft.time;

import java.util.Iterator;

/**
 * 
 * @author mike
 *
 */
public interface Recurrent
{

  /**
   * Provide the Instant of the next recurrence strictly after the given mark
   * 
   * @param mark
   * @return
   */
  Instant next(Instant mark);
  
  /**
   * Provide the set of recurrences within the specified interval
   * 
   * @param mark
   * @return
   */
  Iterator<Instant> iterator(Interval interval);
}
