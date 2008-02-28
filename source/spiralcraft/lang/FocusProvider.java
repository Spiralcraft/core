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
package spiralcraft.lang;

/**
 * <p>A Component which binds itself into the Focus chain in order to 
 *  expose its data and functionality to users of the spiralcraft.lang 
 *  expression language.
 * </p>
 *  
 * @author mike
 *
 */
public interface FocusProvider<T>
{
  /** 
   * <p>Bind to the specified Focus, returning a new Focus that has as its 
   *   parent the specified Focus. 
   * </p>
   * 
   * <p>The new Focus becomes the next link in the Focus chain, and may
   *   provide access to an arbitrary set of Channels. 
   * </p>
   * 
   * <p>If bindFocus is called more than once, Components should ensure that 
   *   all cached bindings created as a result of previous bindFocus calls
   *   are removed.
   * </p>
   * 
   * @return The new Focus
   * @throws BindException if an error occurs when creating the Focus
   */
  Focus<T> createFocus(Focus<?> parent)
    throws BindException;
}
