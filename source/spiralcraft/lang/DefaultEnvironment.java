package spiralcraft.lang;

import java.util.HashMap;
import java.util.Map;

import spiralcraft.util.ArrayUtil;

public class DefaultEnvironment
  implements Environment
{
  private Map _names;
  private Attribute[] _attributes;
  private final Environment _primary;

  public DefaultEnvironment()
  { _primary=null;
  }

  /**
   * Construct a default Environment which augments
   *   the supplied primary Environment. Names
   *   that already exist in the primary cannot be
   *   used in this Environment. 
   */
  public DefaultEnvironment(Environment primary)
  { _primary=primary;
  }

  public Optic resolve(String name)
  { 
    Optic optic=null;
    if (_primary!=null)
    { optic=_primary.resolve(name);
    }
    
    if (optic==null)
    {      
      if (_names!=null) 
      { 
        Attribute attrib=(Attribute) _names.get(name);
        if (attrib!=null)
        { optic=attrib.getOptic();
        }
      }
    }
    return optic;
  }

  public void setAttributes(Attribute[] val)
  { 
    _attributes=val;
    if (_names==null)
    { _names=new HashMap();
    }
    for (int i=0;i<_attributes.length;i++)
    { 
      String name=_attributes[i].getName();
      if (_names.get(name)!=null)
      { throw new IllegalArgumentException("Duplicate name "+name);
      }
      if (_primary!=null && _primary.resolve(name)!=null)
      { throw new IllegalArgumentException("Duplicate name "+name);
      }
      _names.put(name,_attributes[i]);
    }
  }

  public Attribute[] getAttributes()
  { return _attributes;
  }

  public String[] getNames()
  { 
    
    String[] names=null;
    if (_names!=null)
    { 
      names=new String[_names.size()];
      _names.keySet().toArray(names);
    }

    if (_primary!=null)
    { 
      if (names==null)
      { return _primary.getNames();
      }
      else
      { return (String[]) ArrayUtil.appendArrays(_primary.getNames(),names);
      }
    }
    return names;
  }
  
}
