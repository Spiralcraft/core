//
// Copyright (c) 1998,2007 Michael Toth
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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A node of a doubly linked Tree 
 */
public class LinkedTree<T>
  implements
    Tree<LinkedTree<T>,T>
    ,Iterable<LinkedTree<T>>
{
  private T object;
  private final ArrayList<LinkedTree<T>> children
    =new ArrayList<LinkedTree<T>>();
  
  private LinkedTree<T> parent;
  
  public LinkedTree()
  {
  }
  
  public LinkedTree(T object)
  { this.object=object;
  }
  
  public void set(T object)
  { this.object=object;
  }
  
  public T get()
  { return object;
  }
  
  public LinkedTree<T> getParent()
  { return parent;
  }
  
  public void setParent(LinkedTree<T> parent)
  { this.parent=parent;
  }
  
  public void addChild(LinkedTree<T> child)
  { 
    children.add(child);
    child.setParent(this);
  }
  
  public void removeChild(LinkedTree<T> child)
  { children.remove(child);
  }
  
  public boolean isLeaf()
  { return children.size()==0;
  }
  
  public Iterator<LinkedTree<T>> iterator()
  { return children.iterator();
  }
  
  @SuppressWarnings("unchecked") // Array creation
  public LinkedTree<T>[] getChildren()
  { 
    LinkedTree<T>[] ret=new LinkedTree[children.size()];
    children.toArray(ret);
    return ret;
  }
  
  
}
