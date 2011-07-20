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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import spiralcraft.util.string.StringUtil;

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

  public LinkedTree(T object,LinkedTree<T> ... children)
  { 
    this.object=object;
    if (children!=null)
    {
      for (LinkedTree<T> child:children)
      { addChild(child);
      }
    }
  }
  
  @Override
  public void set(T object)
  { this.object=object;
  }
  
  @Override
  public T get()
  { return object;
  }
  
  @Override
  public LinkedTree<T> getParent()
  { return parent;
  }
  
  @Override
  public void setParent(LinkedTree<T> parent)
  { this.parent=parent;
  }
  
  @Override
  public void addChild(LinkedTree<T> child)
  { 
    children.add(child);
    child.setParent(this);
  }
  
  @Override
  public void removeChild(LinkedTree<T> child)
  { children.remove(child);
  }
  
  @Override
  public boolean isLeaf()
  { return children.size()==0;
  }
  
  @Override
  public Iterator<LinkedTree<T>> iterator()
  { return children.iterator();
  }
  
  @Override
  @SuppressWarnings("unchecked") // Array creation
  public LinkedTree<T>[] getChildren()
  { 
    LinkedTree<T>[] ret=new LinkedTree[children.size()];
    children.toArray(ret);
    return ret;
  }
  
  @Override
  public String toString()
  { return render(null,0,".").toString();
  }
  
  public Appendable render(Appendable buf,int depth,String prefix)
  { 
    if (buf==null)
    { buf=new StringBuilder();
    }
    try
    {
      StringUtil.repeat(buf,prefix,depth);
      buf.append(object.toString())
        .append("\r\n");
      for (LinkedTree<T> child:children)
      { child.render(buf,depth+1,prefix);
      }
    }
    catch (IOException x)
    { throw new RuntimeException(x);
    }
    return buf;
  }
  
}
