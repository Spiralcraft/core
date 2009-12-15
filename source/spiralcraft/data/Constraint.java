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
package spiralcraft.data;

import java.net.URI;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Functor;


/**
 * <p>A description of a single data element of a Tuple.
 * </p>
 * 
 * <p>
 * </p>
 */
public interface Constraint<T>
  extends Functor<T,Tuple>
{
  /**
   * <p>The FieldSet to which this Constraint belongs. All Constraints
   * belong to a FieldSet.
   * </p>
   */
  FieldSet getFieldSet();
  
  /**
   * <p>The Field, if any, to which this Constraint belongs. 
   * </p>
   */
  FieldSet getField();

  /**
   * The index of the Constraint within the Scheme, which corresponds to the
   *   order of definition. 
   */
  int getIndex();
  
  /**
   * The name of the Constraint, to be used for the programmatic binding of
   *   data aware components to Tuples of this Scheme. Conforms to
   *   the rules for language identifiers.
   */
  String getName();
  
  /**
   * <p>A short description that asserts what the Constraint requires. (eg.
   *   "Must be under 10 digits"
   * </p>
   */
  String getMessage();
  
  
  /**
   * @return This constraint's URI, which is this constraint's name in the
   *   context of the Type that it belongs to. 
   */
  URI getURI();
  
  
  /**
   * <p>Create a Channel that accesses the value of this Constraint in the Tuple
   *   provided by the source. The results of this operation may 
   *   depend on the availability of other resources mapped through the Focus.
   * </p>
   * 
   * @param focus
   * @return A Binding bound to the focus
   */
   @Override
  Channel<T> bindChannel
    (Channel<Tuple> source,Focus<?> focus,Expression<?>[] params)
    throws BindException;
  
  
}
