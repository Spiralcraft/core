package spiralcraft.lang;

import java.util.HashMap;
import java.util.Map;

public class DefaultEnvironment
  implements Environment
{
  private Map _names;
  private Attribute[] _attributes;

  public Optic resolve(String name)
  { 
    if (_names!=null)
    { 
      Attribute attrib=(Attribute) _names.get(name);
      if (attrib!=null)
      { return attrib.getOptic();
      }
    }
    return null;
  }

  public void setAttributes(Attribute[] val)
  { 
    _attributes=val;
    for (int i=0;i<_attributes.length;i++)
    { _names.put(_attributes[i].getName(),_attributes[i]);
    }
  }

  public Attribute[] getAttributes()
  { return _attributes;
  }
  
}
