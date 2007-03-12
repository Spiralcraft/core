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

import spiralcraft.data.transport.Cursor;


/**
 * <P>A horizontal transformation of data described by one FieldSet (the Master
 *   FieldSet) into data described by another FieldSet (the Projection
 *   FieldSet). Each Field of the Projection FieldSet is defined by a field
 *   or expression relative to the Master FieldSet.
 *   
 * <P>The transformation is materialized by creating a Tuple of the Projection
 *   FieldSet for a tuple of the master FieldSet.
 *  
 * @author mike
 */
public interface Projection
  extends FieldSet
{
  /**
   *@return the master fieldSet from which this fieldSet is derived
   */
  public FieldSet getMasterFieldSet();
  
  /**
   * <P>Materialize the projection for a single Tuple by computing the value for
   *   each Field.
   * 
   * <P>If this Projection needs to be materialized for more than one Tuple,
   *   use the <code>bind(Cursor master)</code> method for efficiency.
   * 
   * @return A Tuple which contains the values resulting from the transformation
   *   of the specified masterTuple.
   */
  public Tuple project(Tuple masterTuple)
    throws DataException;
  
  /**
   * Bind a Cursor which materializes this projection to perform a bulk
   *   operation. 
   * 
   *@return A Cursor which provides a Tuple defined by this FieldSet for
   *  every Tuple of the master FieldSet returned by the specified master
   *  Cursor
   */
  public Cursor bind(Cursor master)
    throws DataException;
  
}
