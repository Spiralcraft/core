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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import spiralcraft.util.Path;

/**
 * A Tree with named nodes
 */
public class PathTree<T>
  implements 
    Tree<PathTree<T>,T>
{
  private T object;
  private final ArrayList<PathTree<T>> children
    =new ArrayList<PathTree<T>>();
  
  private PathTree<T> parent;
  
  private final String name;
  private HashMap<String,PathTree<T>> childMap
    =new HashMap<String,PathTree<T>>();
  
  public PathTree(String name)
  { this.name=name;
  }
  
  public String getName()
  { return name;
  }
  
  /**
   * The path from the root node of the tree to this node. The name of the root node
   *   is not included.
   */
  public Path getPath()
  {
    Stack<String> stack=new Stack<String>();
    PathTree<T> node=this;
    while (node.getParent()!=null)
    {
      stack.push(node.getName());
      node=node.getParent();
    }
    String[] pathArray=new String[stack.size()];
    stack.toArray(pathArray);
    return new Path(pathArray,true);
  }
  
  public PathTree<T> getChild(String name)
  { return childMap.get(name);
  }
  
  public void set(T object)
  { this.object=object;
  }
  
  public T get()
  { return object;
  }
  
  public PathTree<T> getParent()
  { return parent;
  }
  
  public void setParent(PathTree<T> parent)
  { this.parent=parent;
  }
  
  public boolean isLeaf()
  { return children.size()==0;
  }
  
  public void addChild(PathTree<T> child)
  {
    PathTree<T> oldChild=getChild(child.getName());
    if (oldChild!=null)
    { removeChild(oldChild);
    }
    
    children.add(child);
    child.setParent(this);
    childMap.put(child.getName(), child);
  }
  
  public void removeChild(PathTree<T> child)
  {
    childMap.remove(child.getName());
    children.remove(child);
  }
  
  public PathTree<T> findDeepestChild(Path path)
  {
    PathTree<T> ret=this;
    for (String element: path)
    { 
      PathTree<T> child=ret.getChild(element);
      if (child==null)
      { return ret;
      }
      else
      { ret=child;
      }
    }
    return ret;
  }
  
  @SuppressWarnings("unchecked") // Array creation
  public PathTree<T>[] getChildren()
  { 
    PathTree<T>[] ret=new PathTree[children.size()];
    children.toArray(ret);
    return ret;
  }
  
  public Iterator<PathTree<T>> iterator()
  { return children.iterator();
  }
  
  @Override
  public String toString()
  { return super.toString()+":["+name+"]";
  }
}
