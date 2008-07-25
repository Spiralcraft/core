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
package spiralcraft.data.core;

import spiralcraft.data.Field;

import spiralcraft.lang.Expression;


/**
 * <p>A Field in a Projection which maps a name to an expression in the context
 * </p>of another data object. 
 * 
 * <p>Used for obtaining multi-attribute values for Keys, and implementing 
 *   field re-mapping.
 * </p>
 * 
 * <p>The associated ProjectionFieldChannel is bound to the master Tuple 
 *   retrieved from the Focus, and not to the Projection itself, permitting
 *   both the retrieval and modification of Tuple data.
 * </p>
 * 
 * 
 * @author mike
 */
public class ProjectionField
  extends FieldImpl
{

  private Expression<?> expression;
  private Field masterField;
  
  public void setExpression(Expression<?> expression)
  { this.expression=expression;
  }
    
  public Expression<?> getExpression()
  { return expression;
  }

  public void setMasterField(
    Field masterField)
  {
    this.masterField=masterField;
    
  }
  
  public Field getMasterField()
  { return masterField;
  }


}



// History
//
// 2008-02-26 mike: Doc., pre integration to ProjectionImpl
//
//   

