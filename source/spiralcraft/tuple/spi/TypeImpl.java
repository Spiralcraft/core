package spiralcraft.tuple.spi;

import spiralcraft.tuple.Type;
import spiralcraft.tuple.Scheme;

public class TypeImpl
  implements Type
{
  private Scheme _scheme;
  private Class _javaClass;
  
  public TypeImpl()
  {
  }
  
  /**
   * Copy constructor
   */
  public TypeImpl(Type type)
  { 
    _scheme=type.getScheme();
    _javaClass=type.getJavaClass();
  }
  
  public Scheme getScheme()
  { return _scheme;
  }

  public void setScheme(Scheme val)
  { _scheme=val;
  }
  
  public Class getJavaClass()
  { return _javaClass;
  }
  
  public void setJavaClass(Class clazz)
  { _javaClass=clazz;
  }
}
