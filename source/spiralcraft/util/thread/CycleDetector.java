//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.util.thread;

import spiralcraft.util.SetStack;

/**
 * Detects cycles using hashCode() and equals()
 */
public class CycleDetector<T>
{
  
  private final ThreadLocal<SetStack<T>> stack
    =new ThreadLocal<SetStack<T>>()
    {
      @Override
      public SetStack<T> initialValue()
      { return new SetStack<T>();
      }
    };
  

  /**
   * Detects a duplicate object in the stack. If there is no duplicate
   *   returns false and pushes the object into the stack. If there is
   *   a duplicate, returns true.
   */
  public boolean detectOrPush(T object)
  { return !stack.get().push(object);
  }

  /**
   * Pops the last element in the stack
   *
   * This should be called inside a 'finally' block to prevent leaks.
   */
  public void pop()
  { 
    SetStack<T> set=stack.get();
    set.pop();
    if (set.isEmpty())
    { stack.remove();
    }
  }
  
  class StackRef
  {
    public T object;
    public StackRef last;
  }
  
}