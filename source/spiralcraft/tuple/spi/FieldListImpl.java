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
package spiralcraft.tuple.spi;

import spiralcraft.tuple.FieldList;
import spiralcraft.tuple.Field;

import spiralcraft.util.KeyedList;
import spiralcraft.util.AutoMap;
import spiralcraft.util.KeyFunction;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A basic, efficient implementation of a FieldList
 */
public class FieldListImpl<F extends FieldImpl>
  extends KeyedList<F>
  implements FieldList<F>
{

  private final KeyedList<F>.Index<Object,F> _nameMap
    =addMap
      (new HashMap<Object,F>()
      ,new KeyFunction<Object,F>()
        {
          public Object key(F value)
          { return value.getName();
          }
        }
      );
  
  public FieldListImpl(int capacity)
  { super(new ArrayList(capacity));
  }

  /**
   * Copy constructor
   */
  public FieldListImpl(FieldList<? extends Field> fieldList)
  { 
    super(new ArrayList(fieldList.size()));
    for (Field field: fieldList)
    { add((F) new FieldImpl(field)); 
    }
  }
  
  public FieldListImpl()
  { super(new ArrayList());
  }
  
  public F findFirstByName(String name)
  { return _nameMap.getOne(name);
  }
  
  public F getField(int index)
  { return get(index);
  }
}
