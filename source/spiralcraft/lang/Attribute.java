//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.lang;

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
    _type=optic.getContentType();
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
      { _optic=new SimpleBinding(_type,null,false);
      }
      catch (BindException x)
      { x.printStackTrace();
      }
    }
  }
}
