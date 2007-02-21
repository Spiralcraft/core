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
package spiralcraft.util;

import java.util.Stack;
import java.util.HashMap;

/**
 * Detects cycles using hashCode() and equals()
 */
public class CycleDetector<T>
{
  private final Stack<T> stack=new Stack<T>();
  private final HashMap<T,T> map=new HashMap<T,T>();

  /**
   * Detects a duplicate object in the stack. If there is no duplicate
   *   returns false and pushes the object into the stack. If there is
   *   a duplicate, returns true.
   */
  public boolean detectOrPush(T object)
  { 
    if (map.get(object)!=null)
    { return true;
    }
    stack.push(object);
    map.put(object,object);    
    // System.err.println(this.toString()+":"+object.toString());
    return false;
  }

  /**
   * Pops the last element in the stack
   *
   * This should be called inside a 'finally' block to prevent leaks.
   */
  public void pop()
  { 
    map.remove(stack.pop());
  }
  
}