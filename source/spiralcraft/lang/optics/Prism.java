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
package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

/**
 * Exposes parts of an object model to access by Expressions by creating
 *   transformations based on elements of Expression syntax.
 */
public interface Prism
{

  /**
   * Generate a new Binding which resolves the name and the given parameter 
   *   expressions against the source Binding and the supplied Focus.
   */
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException;

  /**
   * Decorate the specified optic with a decorator that implements the
   *   specified interface
   */
  public Decorator decorate(Binding source,Class decoratorInterface)
    throws BindException;
  
  /**
   * Return the Java class of the data object accessible through Bindings 
   *   associated with this Prism
   */
  public Class<?> getContentType();
}
