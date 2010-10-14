//
// Copyright (c) 1998,2008 Michael Toth
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

import spiralcraft.lang.Expression;

public interface ProjectionField<T>
  extends Field<T>
{

  /**
   * 
   * @return The Expression associated with this Field
   */
  Expression<?> getExpression();
  
  /**
   * 
   * @return The Source field associated with this ProjectionField, if it 
   *   maps directly onto a field in the projection source.
   */
  Field<?> getSourceField();
}
