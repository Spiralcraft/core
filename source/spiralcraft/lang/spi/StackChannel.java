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
package spiralcraft.lang.spi;


import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;


/**
 * <p>References an object at the top of a stack, to support reentrant
 *   binding
 * </p>
 *  
 */
public class StackChannel<T>
  extends AbstractChannel<T>
  implements Channel<T>
{
  private ObjectReference<T> ref;
  
  public StackChannel(Reflector<T> reflector)
  { super(reflector);
  }
  
  @Override
  public boolean isWritable()
  { return true;
  }
  
  @Override
  public T retrieve()
  { return ref.object;
  }

  @Override
  public boolean store(T val)
  { 
    ref.object=val;
    return true;
  }
  
  
  /**
   * Provide a new local value for use by all outgoing method calls.
   */
  public void push(T val)
  { 
    ObjectReference<T> oldref=ref;
    ObjectReference<T> newref=new ObjectReference<T>();
    newref.object=val;
    newref.prior=oldref;
    ref=newref;
  }
  
  /**
   * Restore the value of this threadLocal before push() was called.
   */
  public void pop()
  { 
    ref=ref.prior;

  }
  
  class ObjectReference<X>
  {
    public ObjectReference<X> prior;
    public X object;
  }

}
