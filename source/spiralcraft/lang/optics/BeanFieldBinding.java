package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;


import java.lang.reflect.Field;

public class BeanFieldBinding
  extends LenseBinding
{

  private final Field _field;

  public BeanFieldBinding
    (Binding source
    ,BeanFieldLense lense
    )
  {
    super(source,lense,null);
    _field=lense.getField();
  }



  public boolean isStatic()
  { return false;
  }

  public synchronized boolean set(Object val)
  {
    try
    { 
      _field.set(getSourceValue(),val);
      return true;
    }
    catch (IllegalAccessException x)
    { return false;
    }
  }

  public String toString()
  { 
    return super.toString()
      +":"+super.toString()
      +":[field="+_field.getName()+" ("+_field.getType()+")]";
  }

}


