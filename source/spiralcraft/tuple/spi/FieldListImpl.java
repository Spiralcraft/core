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
public class FieldListImpl
  extends KeyedList
  implements FieldList
{

  private final KeyedList.Index _nameMap
    =addMap
      (new HashMap()
      ,new KeyFunction()
        {
          public Object key(Object value)
          { return ((Field) value).getName();
          }
        }
      );
  
  public FieldListImpl(int capacity)
  { super(new ArrayList(capacity));
  }

  public FieldListImpl()
  { super(new ArrayList());
  }
  
  public Field findFirstByName(String name)
  { return (Field) _nameMap.getOne(name);
  }
}
