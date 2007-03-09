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
 * A FieldSet derived from a "master" FieldSet which provides a facility 
 *   to materialize this transformation by creating a Tuple of the Projection
 *   FieldSet for each tuple of the master FieldSet.
 *  
 * A MultiProjection can be compared to a SQL SELECT expression list.
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
   *@return A Cursor which provides a Tuple defined by this FieldSet for
   *  every Tuple of the master FieldSet returned by the specified master Cursor
   */
  public Cursor bind(Cursor master)
    throws DataException;
  
}
