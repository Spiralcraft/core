package spiralcraft.lang;

import spiralcraft.lang.optics.SimpleOptic;

/**
 * Maps a name to an Optic
 */
public class Attribute
{
  private String _name;
  private Optic _optic;
  private Class _type;

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
    { _optic=OpticFactory.decorate(new SimpleOptic(_type));
    }
  }
}
