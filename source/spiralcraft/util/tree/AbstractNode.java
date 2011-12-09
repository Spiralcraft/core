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
package spiralcraft.util.tree;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;


import spiralcraft.util.EmptyIterator;
import spiralcraft.util.tree.Tree;

/**
 * Represents a portion of an XML document
 */
public abstract class AbstractNode<Nc extends AbstractNode<Nc,T>,T>
  implements Tree<Nc,T>
{
  private LinkedList<Nc> _children;
  private Nc _parent;
  private T _peer;


  /**
   * Associate an application specific Object with this
   *   node.
   */
  @Override
  public void set(T peer)
  { _peer=peer;
  }
  
  /**
   * Return the application specific Object
   *   associated with this node.
   *   
   */
  @Override
  public T get()
  { return _peer;
  }
  
  @Override
  public boolean isLeaf()
  { return _children==null || _children.isEmpty();
  }
  
  @Override
  public void removeChild(Nc child)
  { _children.remove(child);
  }

  @SuppressWarnings("unchecked")
  public void remove()
  { 
    if (_parent!=null)
    { _parent.removeChild((Nc) this);
    }
  }

  /**
   * Returns a direct reference to the internal list of child Nodes
   * 
   * @return
   */
  public List<Nc> getChildList()
  { return _children;
  }
  
  public int getChildCount()
  { 
    if (_children!=null)
    { return _children.size();
    }
    else
    { return 0;
    }
  }
  
  @Override
  public Iterator<Nc> iterator()
  { 
    if (_children!=null)
    { return _children.iterator();
    }
    else
    { return new EmptyIterator<Nc>();
    }
  }
  
  /**
   * Returns a new copy of the list of child nodes
   */
  @SuppressWarnings("unchecked")
  @Override
  public Nc[] getChildren()
  { 
    if (_children!=null)
    { return (Nc[]) _children.toArray(new AbstractNode[_children.size()]);
    }
    else
    { return null;
    }
  }

  @SuppressWarnings("unchecked") // Runtime type check
  public <X extends Nc> List<X> getChildren(Class<X> type)
  { 
    if (_children==null)
    { return null;
    }

    List<X> ret=new ArrayList<X>();
    for (Nc child:_children)
    {
      if (type.isAssignableFrom(child.getClass()))
      { ret.add((X) child);
      }
    }
    return ret;
  }

  /**
   * Remove children including and after the specified child node. 
   * 
   * @param afterChild
   */
  public void truncate(int index)
  { 
    if (_children!=null)
    { 
      while (_children.size()>index)
      { _children.removeLast();
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void addChild(Nc child)
  { 
    if (_children==null)
    { _children=new LinkedList<Nc>();
    }
    _children.add(child);
    child.setParent((Nc) this);
  }

  public boolean hasChildren()
  { return _children!=null;
  }

  public boolean canHaveChildren()
  { return false;
  }

  @Override
  public void setParent(Nc parent)
  { _parent=parent;
  }

  @Override
  public Nc getParent()
  { return _parent;
  }

  public Nc getChild(int index)
  { 
    if (_children!=null)
    { return _children.get(index);
    }
    else
    { throw new IndexOutOfBoundsException(index+">0");
    }
  }
  

}
