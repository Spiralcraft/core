package spiralcraft.tuple.lang;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Tuple;

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;

public class FieldLense
  implements Lense
{
  private final Field _field;
  private final Prism _prism;
  
  public FieldLense(Field field)
    throws BindException
  { 
    _field=field;

    if (_field.getType().getScheme()!=null)
    { 
      // Re-use the SchemePrisms- weak map?
      _prism
        =new SchemePrism(_field.getType().getScheme());
    }
    else
    {
      _prism=
        OpticFactory.getInstance().findPrism
          (_field.getType().getJavaClass());
    }
  }

  public Field getField()
  { return _field;
  }
  
  public Object translateForGet(Object value,Object[] modifiers)
  { return ((Tuple) value).get(_field);
  }

  public Object translateForSet(Object val,Object[] modifiers)
  { throw new UnsupportedOperationException();
  }
    
  public Prism getPrism()
  { return _prism;
  }
}
