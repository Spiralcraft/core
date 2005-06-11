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
 * A locus for the evaluation of Expressions
 *
 * A Focus references a single subject and is associated with a 
 *   Context. The Context provides access to the 'workspace' of a given
 *   task, and as such will usually contain the subject of the Focus as well
 *   as a number of other named attributes which provide access to data, tools
 *   and resources useful to a given computation.  
 *
 * Expressions bound to a Focus incorporate attributes of the subject
 *   and the Context into traversals, transformations and computations to
 *   create new subjects of Focus. 
 */
public interface Focus
{
    
  /**
   * Return the Context of this Focus (the 'workspace' in which the computation
   *   is being performed).
   */
  Context getContext();

  /**
   * Return the subject of expression evaluation
   */
  Optic getSubject();

  /**
   * Find a Focus using its well known name.
   */
  Focus findFocus(String name);

  /**
   * Return this Focus's parent Focus.
   */
  Focus getParentFocus();

  /**
   * Return a Channel, which binds the Expression to this Focus. It may be
   *   useful for implementations to cache Channels to avoid creating
   *   multiple channels for the same Expression.
   */
  Channel bind(Expression expression)
    throws BindException;
}
