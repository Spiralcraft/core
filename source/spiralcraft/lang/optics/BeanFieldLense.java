package spiralcraft.lang.optics;

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import java.lang.reflect.Field;

class BeanFieldLense
  implements Lense
{
  private final Field _field;
  private final Prism _prism;
  
  public BeanFieldLense(Field field)
    throws BindException
  { 
    _field=field;
    _prism=OpticFactory.getInstance().findPrism(_field.getType());
  }

  public Field getField()
  { return _field;
  }

  public Object translateForGet(Object value,Optic[] modifiers)
  { 
    try
    { return _field.get(value);
    }
    catch (IllegalAccessException x)
    { return null;
    }
  }

  public Object translateForSet(Object val,Optic[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Prism getPrism()
  { return _prism;
  }

}

