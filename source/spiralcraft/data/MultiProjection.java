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
 * A FieldSet which combines fields from one or more other FieldSets into a
 *   horizontal union, and provides a facility to materialize this transformation
 *   via a Cursor of Tuples that is bound to a set of component Cursors that provide
 *   access to data for each of the component FieldSets.<P>
 *  
 * A MultiProjection can be compared to a SQL SELECT expression list.
 * 
 * @author mike
 */
public interface MultiProjection
  extends FieldSet
{
  public FieldSet getComponentByIndex(int index);
  
  public FieldSet getComponentByName(String name);
  
  public int getComponentCount();
  
  public FieldSet[] getComponents();
  
  public Cursor bind(Cursor[] components);
  
}
