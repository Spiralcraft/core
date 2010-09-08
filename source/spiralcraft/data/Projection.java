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
package spiralcraft.data;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.ChannelFactory;

/**
 * <p>A horizontal transformation of data in an arbitrary form into 
 *   a Tuple described by a FieldSet (the Projection). Each Field of the
 *   Projection is defined by some expression evaluated against the data
 *   available from the target of the projection. 
 * </p>
 *   
 * <p>The transformation is materialized by creating a Tuple of the Projection
 *   FieldSet for each instance of the target type. 
 * </p>
 *  
 * @author mike
 */
public interface Projection<T>
  extends FieldSet,ChannelFactory<Tuple,T>
{
  
  /**
   * <p>Bind the Projection to a Focus which sources the master data.
   * </p>
   * 
   * <p>The Tuple provided by the Channel is a view into the current
   *   values(s) provided by the source object, and is mutable- ie. will change
   *   along with the backing values. Therefore, the user should call snapshot() 
   *   if the value is to be used beyond the immediate context.
   * </p>
   */
  @Override
  public Channel<Tuple> bindChannel
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?>[] params
    )
  throws BindException;

  /**
   * 
   * @return The array of expressions evaluated against the master data that
   *   defines this Projection.
   */
  Expression<?>[] getTargetExpressions();
  
  /**
   *@return an Iterable which provides access to 
   *   fields in order of their indexes
   */
  @Override
  Iterable<? extends ProjectionField<?>> fieldIterable();  
  
}
