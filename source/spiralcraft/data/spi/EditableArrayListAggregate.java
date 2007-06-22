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
package spiralcraft.data.spi;

import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Aggregate;

import spiralcraft.data.Type;

/**
 * Holds a aggregation of objects of a common type.
 */
public class EditableArrayListAggregate<T>
  extends ArrayListAggregate<T>
  implements EditableAggregate<T>
{
  public EditableArrayListAggregate(Type type)
  { super(type);
  }
  
  public void add(T val)
  { list.add(val);
  }
  
  public boolean isMutable()
  { return true;
  }
  
  public void addAll(Aggregate<T> values)
  {
    for (T value: values)
    { list.add(value);
    }
  }
  
}