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
package spiralcraft.tuple;

import java.util.List;

/**
 * A list of Fields 
 */
public interface FieldList<F extends Field>
  extends List<F>
{
  /**
   *@return The first Field with the specified name, or  null
   *   if none was found
   */
  F findFirstByName(String name);
  
  /**
   *@return The Field at the given position.
   */
  F getField(int pos);
  
}
