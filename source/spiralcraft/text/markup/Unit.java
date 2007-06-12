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
package spiralcraft.text.markup;

import java.util.LinkedList;
import java.util.List;

/**
 * A Unit of marked up text
 */
public abstract class Unit<T extends Unit<T>>
{
  protected LinkedList<T> children;
  private Unit<T> _parent;
  private String _name;
  
  public void addChild(T child)
  { 
    if (children==null)
    { children=new LinkedList<T>();
    }
    children.add(child);
    child.setParent(this);
  }
  
  public void setParent(Unit<T> parent)
  { _parent=parent;
  }
  
  public Unit<T> getParent()
  { return _parent;
  }
  
  public String getName()
  { return _name;
  }
  
  protected void setName(String name)
  { _name=name;
  }

  public List<T> getChildren()
  { return children;
  }  
  
  public boolean isOpen()
  { return false;
  }
  
  public void close()
    throws MarkupException
  {
  }

}
