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
 * <p>A directive sent down a Component containership hierarchy 
 *   to trigger behavior.
 * </p>
 * 
 * @author mike
 */
public abstract class Message
{
  private static volatile int NEXT_TYPE_ID=0;
  
  protected boolean multicast;
  protected boolean outOfBand;
  
  /**
   * Multicast messages will be sent to all descendants of their target
   * 
   * @return
   */
  public boolean isMulticast()
  { return multicast;
  } 

  /**
   * "Out Of Band" messages do not affect the state frame
   * 
   * @return
   */
  public boolean isOutOfBand()
  { return outOfBand;
  } 
  
  public abstract Type getType();
  
  public static class Type
  { 
    public final int ID=NEXT_TYPE_ID++;
  }
}
