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
package spiralcraft.lang;

/**
 * A Context provides a set of named Optics (data channels) for use in
 *   Expressions. A Context is normally associated with one or more Foci
 *   and provides references to contextual data to use in computations. 
 *
 * The particulars of the Context names and what they reference are application
 *   specific- a Context is a generic 'container' for expression evaluation.
 *
 */
public interface Context
{
  /**
   * Resolve the name by returning the Optic which corresponds to this name
   */
  public <X> Optic<X> resolve(String name);

  /**
   * Return the attribute names in this Context
   */
  public String[] getNames();  
}
