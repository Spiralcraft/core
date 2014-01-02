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

import java.util.Iterator;
import java.util.Stack;

public class IteratorStack<T>
  implements Iterator<T>
{
  
  private final Stack<Iterator<T>> stack=new Stack<Iterator<T>>();
  private boolean done;
  
  @SuppressWarnings("unchecked")
  public IteratorStack(Iterator<T>... iterators)
  { 
    for (Iterator<T> iter : iterators)
    { stack.add(iter);
    }
  }

  public void push(Iterator<T> next)
  { 
    if (!done)
    { stack.push(next);
    }
    else
    { 
      throw new IllegalStateException
        ("Cannot push an Iterator into a completed IteratorStack");
    }
  }
  
  @Override
  public boolean hasNext()
  { 
    if (done)
    { return false;
    }
    
    if (!stack.empty())
    {
      if (stack.peek().hasNext())
      { return true;
      }
      else
      { 
        stack.pop();
        if (hasNext())
        { return true;
        }
        else
        { 
          done=true;
          return false;
        }
      }
    }
    else
    { 
      done=true;
      return false;
    }
    
  }
  

  @Override
  public T next()
  {
    if (hasNext())
    { return stack.peek().next();
    }
    else
    { return null;
    }
  }

  @Override
  public void remove()
  {
    if (hasNext())
    { stack.peek().remove();
    }    
  }

}
