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

import java.util.Iterator;

import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Aggregate;

import spiralcraft.data.Type;

/**
 * Holds a aggregation of objects of a common type.
 */
public class EditableKeyedListAggregate<T>
  extends KeyedListAggregate<T>
  implements EditableAggregate<T>
{
  public EditableKeyedListAggregate(Type<?> type)
  { super(type);
  }
  
  public EditableKeyedListAggregate(Aggregate<T> original)
  { super(original);
  }

  @Override
  public void add(T val)
  { list.add(val);
  }
  
  @Override
  public boolean isMutable()
  { return true;
  }
  
  @Override
  public void addAll(Aggregate<T> values)
  {
    for (T value: values)
    { list.add(value);
    }
  }
  
  @Override
  public void addAll(Iterator<T> values)
  {
    while (values.hasNext())
    { list.add(values.next());
    }
  }
  
  @Override
  public void remove(T value)
  { list.remove(value);
  }
  
  public void replace(T oldValue,T newValue)
  { list.replace(oldValue,newValue);
  }
  
  @Override
  public void clear()
  { list.clear();
  }
}