//
// Copyright (c) 2009,2009 Michael Toth
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
// "AS
package spiralcraft.util;

import java.util.HashSet;
import java.util.Stack;

/**
 * A stack which does not allow duplicate elements
 * 
 * @author mike
 *
 */
public class SetStack<T>
{

  private Stack<T> stack=new Stack<T>();
  private HashSet<T> set=new HashSet<T>();
  
  
  /**
   * Adds the element if it is not already in the stack
   * @param element
   * @return true, if the element was added, false if it was already in the
   *   stack
   */
  public boolean push(T element)
  { 
    if (set.contains(element))
    { return false;
    }
    else
    { 
      set.add(element);
      stack.push(element);
      return true;
    }
    
  }
  
  public T pop()
  {
    T element=stack.pop();
    set.remove(element);
    return element;
  }
  
  public int size()
  { return stack.size();
  }
  
  public boolean isEmpty()
  { return stack.isEmpty();
  }
}
