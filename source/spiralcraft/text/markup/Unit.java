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

import spiralcraft.text.ParsePosition;

/**
 * A Unit of marked up text
 */
public abstract class Unit<T extends Unit<T>>
{
  protected LinkedList<T> children;
  protected final T parent;
  private String _name;
  private ParsePosition position;
  
  @SuppressWarnings("unchecked") // addChild(this)- bi-di link assumes subclass
  public Unit(T parent)
  { 
    this.parent=parent;
    if (this.parent!=null)
    { this.parent.addChild((T) this);
    }
  }
  
  void addChild(T child)
  { 
    if (children==null)
    { children=new LinkedList<T>();
    }
    children.add(child);
  }
  
  public ParsePosition getPosition()
  { return position;
  }
  
  public void setPosition(ParsePosition position)
  { this.position=position;
  }
  
  /**
   * Find a unit among this Unit's ancestors/containers
   * 
   * @param <X>
   * @param clazz
   * @return The unit with the specific class, or null if none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  public <X extends Unit<T>> X findUnit(Class<X> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findUnit(clazz);
    }
    else
    { return null;
    }
  }

  public T getParent()
  { return parent;
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
