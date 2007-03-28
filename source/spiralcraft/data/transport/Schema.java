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
package spiralcraft.data.transport;

import spiralcraft.data.Type;

/**
 * The collection of Types associated with an atomic unit of data storage.
 */
public class Schema
{
  private Type[] types;
  
  public Type[] getTypes()
  { return types;
  }
  
  public void setTypes(Type[] types)
  { this.types=types;
  }
}
