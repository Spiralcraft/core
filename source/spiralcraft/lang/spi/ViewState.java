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
package spiralcraft.lang.spi;

/**
 * Keeps state across a finite series of arbitrary events.
 * 
 * @author mike
 *
 * @param <T>
 */
public class ViewState<T>
{ 
  public ViewState()
  {
  }
  
  /**
   * Set when input should be considered
   */
  public volatile boolean frameChanged;
  
  /**
   * Set when output should be fully computed
   */
  public volatile boolean checkpoint;

  /**
   * An opaque representation of the running state of the "view" to which
   *   this ViewState has been allocated.
   */
  public volatile T data;
  
  @Override
  public String toString()
  { return super.toString()+": "+frameChanged+", "+checkpoint+", "+data;
  }
      
}