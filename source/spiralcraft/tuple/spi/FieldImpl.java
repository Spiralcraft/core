package spiralcraft.tuple.spi;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Type;

/**
 * A basic implementation of a Field, to be used with the SchemeImpl class for
 *   the manual construction of Schemes (progammatically or via assembly)
 */
public class FieldImpl
  implements Field
{
  private Scheme _scheme;
  private int _index;
  private String _name;
  private Type _type;  

  public FieldImpl()
  {
  }
  
  /**
   * Copy constructor
   */
  public FieldImpl(Field field)
  { 
    _name=field.getName();
    _type=new TypeImpl(field.getType());
  }
  
  public Scheme getScheme()
  { return _scheme;
  }

  
  public int getIndex()
  { return _index;
  }
  
  public String getName()
  { return _name;
  }

  public void setName(String name)
  { _name=name;
  }
  
  public void setType(Type type)
  { _type=type;
  }

  public Type getType()
  { return _type;
  }

  /**
   * Used by SchemeImpl to set the index
   */
  void setIndex(int index)
  { _index=index;
  }

  /**
   * Used by SchemeImpl to set the Scheme
   */
  void setScheme(Scheme scheme)
  { _scheme=scheme;
  }
  
}
