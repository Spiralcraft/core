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


/**
 * Thrown when some operation associated with a Type is applied to
 *   an operand associated with another Type that is incompatible with the
 *   first. 
 */
public class TypeMismatchException
  extends DataException
{
  private static final long serialVersionUID=1;	
  
  public TypeMismatchException(String message)
  { super(message);
  }
  
  public TypeMismatchException(String message,Type<?> formalType,Type<?> actualType)
  { super(message
          +": expected "
          +(formalType!=null?formalType.getURI():"(null)")
          +" but found "
          +(actualType!=null?actualType.getURI():"(null)")
          );
  }

}
