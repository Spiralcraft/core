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
    for (int i=0;i<pathArray.length;i++)
    { pathArray[i]=stack.pop();
    }
    return new Path(pathArray,'/',true,!isLeaf());
  }
  
  public PathTree<T> getChild(String name)
  { return childMap.get(name);
  }

  @Override
  public PathTree<T> getRoot()
  { 
    if (parent==null)
    { return this;
    }
    else
    { return parent.getRoot();
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
  public PathTree<T> getParent()
  { return parent;
  }
  
  @Override
  public void setParent(PathTree<T> parent)
  { this.parent=parent;
  }
  
  @Override
  public boolean isLeaf()
  { return children.size()==0;
  }
  
  @Override
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
  
  @Override
  public void removeChild(PathTree<T> child)
  {
    childMap.remove(child.getName());
    children.remove(child);
  }
  
  /**
   * Returns the deepest child of the tree that is at the same depth 
   *   or shallower than the specified path
   * 
   * @param path
   * @return
   */
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
  
  @Override
  @SuppressWarnings("unchecked") // Array creation
  public PathTree<T>[] getChildren()
  { 
    PathTree<T>[] ret=new PathTree[children.size()];
    children.toArray(ret);
    return ret;
  }
  
  @Override
  public Iterator<PathTree<T>> iterator()
  { return children.iterator();
  }
  
  @Override
  public String toString()
  { return super.toString()+":["+name+": "+this.object+"]";
  }
  
  public String format()
  {
    StringBuffer out=new StringBuffer();
    format(out,"\r\n");
    return out.toString();
  }
  
  public void format(Appendable out,String prefix)
  { 
    try
    {
      out.append(prefix);
      out.append(toString());
      for (PathTree<T> child: children)
      { child.format(out,prefix+" | ");
      }
    }
    catch (IOException x)
    { throw new RuntimeException(x);
    }
  }
}
