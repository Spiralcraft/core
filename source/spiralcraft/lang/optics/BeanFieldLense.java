package spiralcraft.lang.optics;

import java.lang.reflect.Field;

class BeanFieldLense
  implements Lense
{
  private final Field _field;
  
  public BeanFieldLense(Field field)
  { _field=field;
  }

  public Field getField()
  { return _field;
  }

  public Object translateForGet(Object value,Object[] modifiers)
  { 
    try
    { return _field.get(value);
    }
    catch (IllegalAccessException x)
    { return null;
    }
  }

  public Object translateForSet(Object val,Object[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Class getTargetClass()
  { return _field.getType();
  }

}

