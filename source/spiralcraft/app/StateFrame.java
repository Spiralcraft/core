//
// Copyright (c) 2009 Michael Toth
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
 * <p>Identifies a sequence of Events during which the state tree is not
 *   updated externally.
 * </p>
 * 
 * <p>A State stores the last StateFrame in which it was visited.
 * </p>
 * 
 * @author mike
 *
 */
public class StateFrame
{
  private static volatile int NEXT_ID=1;
  
  private final int id=NEXT_ID++;
  
  @Override
  public String toString()
  { return super.toString()+" #"+id;
  }
  
}
