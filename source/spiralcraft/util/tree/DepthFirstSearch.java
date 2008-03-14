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

import java.util.Iterator;
import java.util.Stack;

import spiralcraft.log.ClassLogger;

/**
 * Implements a Depth First Search (preorder) algorithm on a Tree
 */
public class DepthFirstSearch<T extends Tree<T,?>>
  implements Iterable<T>
{

  static final ClassLogger log=ClassLogger.getInstance(DepthFirstSearch.class);

  

  
  private final T tree;
  
  public DepthFirstSearch(T tree)
  { this.tree=tree;
  }
  
  public Iterator<T> iterator()
  { return new DFSIterator();
  }
  
  
  
  class DFSIterator
    implements Iterator<T>
  {
    private final Stack<T> stack=new Stack<T>();
    private T lastNode;

    public DFSIterator()
    { stack.push(tree);
    }

    public boolean hasNext()
    { return !stack.isEmpty();
    }
    
    public T next()
    {
      lastNode=stack.pop();
      
      T[] children=lastNode.getChildren();
     
      for (int i=children.length;i-->0;)
      { 
        T child=children[i];
//        log.fine("Pushing "+child);
        stack.push(child);
      }
      return lastNode;
    }
    
    public void remove()
    { 
      if (lastNode!=null && lastNode.getParent()!=null)
      { lastNode.getParent().removeChild(lastNode);
      }
    }
    
  }
}
