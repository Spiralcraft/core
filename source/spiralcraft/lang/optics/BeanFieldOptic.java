package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;


import java.lang.reflect.Field;

public class BeanFieldOptic
  extends AbstractOptic
{

  private final Optic _source;
  private final Field _field;

  public BeanFieldOptic
    (Optic source
    ,Field field
    )
  {
    _source=source;
    _field=field;
  }

  public Class getTargetClass()
  { return _field.getType();
  }

  public Object get()
  { 
    try
    { return _field.get(_source.get());
    }
    catch (IllegalAccessException x)
    { return null;
    }
  }

  public synchronized boolean set(Object val)
  {
    try
    { 
      _field.set(_source.get(),val);
      return true;
    }
    catch (IllegalAccessException x)
    { return false;
    }
  }

  public String toString()
  { 
    return super.toString()
      +":"+_source.toString()
      +":[field="+_field.getName()+" ("+_field.getType()+")]";
  }
}
