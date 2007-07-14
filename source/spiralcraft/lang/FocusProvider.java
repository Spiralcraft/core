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
 * A Component capable of creating a Focus in order to expose its data and 
 *  functionality to the spiralcraft.lang expression language.
 *  
 * @author mike
 *
 */
public interface FocusProvider
{
  /** 
   * <P>Create a new Focus, that has as its parent the specified Focus and that
   *   will register itself for resolution by the <code>[<i>alias</i>]</code> 
   *   operator. 
   * 
   * @param parent The parent Focus
   * @param name The name this Focus can be addressed using the Focus
   *                 resolution operator, ( ie. <code>[<i>myWidget</i>]</code>)
   * @return The new Focus
   * @throws BindException if an error occurs when creating the Focus
   */
  Focus<?> createFocus(Focus<?> parent,String name)
    throws BindException;
}
