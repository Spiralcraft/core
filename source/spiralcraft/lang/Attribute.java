package spiralcraft.lang;

import spiralcraft.lang.optics.SimpleOptic;
import spiralcraft.lang.optics.SimpleBinding;

/**
 * Maps a name to an Optic. Used primarily in an AttributeContext
 */
public class Attribute
{
  private String _name;
  private Optic _optic;
  private Class _type;

  public Attribute()
  {
  }

  public Attribute(String name,Optic optic)
  { 
    _name=name;
    _optic=optic;
    _type=optic.getTargetClass();
  }

  public Attribute(String name,Class type)
  { 
    _name=name;
    _type=type;
  }

  public Optic getOptic()
  { 
    if (_optic==null)
    { createOptic();
    }
    return _optic;
  }

  public String getName()
  { return _name;
  }

  public void setName(String name)
  { _name=name;
  }

  public void setType(Class type)
  { _type=type;
  }

  public void setOptic(Optic val)
  { _optic=val;
  }

  private synchronized void createOptic()
  { 
    if (_optic==null)
    { 
      try
      { _optic=new SimpleOptic(new SimpleBinding(_type,null,false));
      }
      catch (BindException x)
      { x.printStackTrace();
      }
    }
  }
}
