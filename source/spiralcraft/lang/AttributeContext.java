package spiralcraft.lang;

import java.util.HashMap;
import java.util.Map;

import spiralcraft.util.ArrayUtil;

import spiralcraft.lang.optics.SimpleBinding;

/**
 * A Context defined by a set of Attributes
 */
public class AttributeContext
  implements Context
{
  private Map _names;
  private Attribute[] _attributes;
  private final Context _parent;

  public AttributeContext()
  { _parent=null;
  }

  /**
   * Construct a Context which adds to the namespace of a parent
   *   Context.
   */
  public AttributeContext(Context parent)
  { _parent=parent;
  }

  public Optic resolve(String name)
  { 
    Optic optic=null;
    if (_names!=null) 
    { 
      Attribute attrib=(Attribute) _names.get(name);
      if (attrib!=null)
      { optic=attrib.getOptic();
      }
    }

    
    if (optic==null)
    { 
      if (_parent!=null)
      { optic=_parent.resolve(name);
      }
    }
    return optic;
  }

  public void setAttributes(Attribute[] val)
  { 
    _attributes=val;
    if (_attributes==null)
    { _names=null;
    }
    else
    {
      if (_names==null)
      { _names=new HashMap();
      }
      for (int i=0;i<_attributes.length;i++)
      { 
        String name=_attributes[i].getName();
        if (_names.get(name)!=null)
        { throw new IllegalArgumentException("Duplicate name "+name);
        }
        if (_parent!=null && _parent.resolve(name)!=null)
        { throw new IllegalArgumentException("Duplicate name "+name);
        }
        _names.put(name,_attributes[i]);
      }
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

    if (_parent!=null)
    { 
      if (names==null)
      { return _parent.getNames();
      }
      else
      { return (String[]) ArrayUtil.appendArrays(_parent.getNames(),names);
      }
    }
    return names;
  }
  
  /**
   * Put specified Optic into the Context 
   */
  public void putOptic(String name,Optic optic)
  {
    if (_names==null)
    { setAttributes(new Attribute[] {new Attribute(name,optic)});
    }
    else
    { 
      Attribute attrib=(Attribute) _names.get(name);
      if (attrib==null)
      { 
        attrib=new Attribute(name,optic);
        _attributes=(Attribute[]) ArrayUtil.append(_attributes,attrib);
        _names.put(name,attrib);
      }
      else
      { attrib.setOptic(optic);
      }
    }
  }
  
  /**
   * Put a Java object into the context under the specified name
   */
  public void putObject(String name,Object val)
    throws BindException
  { putOptic(name,new SimpleBinding(val,true));
  }
  
}
