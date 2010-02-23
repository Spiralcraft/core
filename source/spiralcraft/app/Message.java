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
  protected boolean multicast;
  
  public boolean isMulticast()
  { return multicast;
  } 
  
  public abstract Type getType();
  
  public static class Type
  { }
}
