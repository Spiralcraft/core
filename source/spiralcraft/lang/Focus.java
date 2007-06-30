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
 * <P>A locus for the evaluation of an Expression.
 * 
 * <P>When an Expressions are evaluated, the Focus is the starting point for
 *   the resolution of identifiers in the Expression language syntax.
 *   
 * <P>A hierarchy of Focus objects comprises a chain that allows components
 *   at a global scope to publish themselves under names that can be reference
 *   by Expressions acting at a local scope.
 * 
 * <P>The subject of a Focus is the 
 *
 * A Focus references a single subject and a context. The context provides
 *   access to a 'pinned', or more general scope of evaluation in circumstances
 *   where the evaluation of an expression traverses recursively deeper 
 *
 * Expressions bound to a Focus incorporate attributes of the subject
 *   and the context into traversals, transformations and computations to
 *   create new subjects of Focus. 
 */
public interface Focus<T>
{
    
  /**
   * Return the Context of this Focus (the 'workspace' in which the computation
   *   is being performed). A Focus inherits its parent's Context if it does
   *   not have one of its own.
   */
  Channel<?> getContext();

  /**
   * Return the subject of expression evaluation
   */
  Channel<T> getSubject();

  /**
   * Find a Focus using an application specific public name.
   */
  Focus<?> findFocus(String name);

  /**
   * Return this Focus's parent Focus.
   */
  Focus<?> getParentFocus();

  /**
   * Return a Channel, which binds the Expression to this Focus. It may be
   *   useful for implementations to cache Channels to avoid creating
   *   multiple channels for the same Expression.
   */
  <X> Channel<X> bind(Expression<X> expression)
    throws BindException;
}
