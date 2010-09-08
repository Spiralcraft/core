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
package spiralcraft.app.components.old;

import java.util.ArrayList;
import java.util.Iterator;

import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.app.spi.ValueState;

public class IterationState<T>
  implements State,Iterable<ValueState<T>>
{
  
  private final int grandchildCount;
  private final State parent;
  private final String id;
  
  private ArrayList<ValueState<T>> children
    =new ArrayList<ValueState<T>>();
  
  private volatile StateFrame lastFrame;
  private volatile boolean valid;
  
  private int[] path;
  
  public IterationState(int grandchildCount,State parent,String id)
  { 
    this.grandchildCount=grandchildCount;
    this.parent=parent;
    this.id=id;
  }

  public boolean frameChanged(StateFrame frame)
  {
    if (lastFrame!=frame)
    { 
      lastFrame=frame;
      valid=false;
      return true;
    }
    return false;
  }
  
  public boolean isValid()
  { return valid;
  }
  

  void setValid(boolean valid)
  { this.valid=valid;
  }
  
  @Override
  public State getParent()
  { return parent;
  }
  
  
  protected ValueState<T> ensureChild(int index,T memento)
  { 
    while (children.size()<=index)
    { 
      ValueState<T> child=new ValueState<T>(grandchildCount,this,id);
      children.add(child);
    }

    ValueState<T> child=children.get(index);
    child.setValue(memento);
    return child;
  }
  
  @Override
  public State getChild(int index)
  { return children.size()>index?children.get(index):null;
  }
  
  public int getChildCount()
  { return children.size();
  }
  
  @Override
  public Iterator<ValueState<T>> iterator()
  { return children.iterator();
  }
  
  public void trim(int size)
  { 
    while (children.size()>size)
    { children.remove(children.size()-1);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> X findState(
    Class<X> clazz)
  { 
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.findState(clazz);
    }
    else
    { return null;
    }

  }

  @Override
  public State getAncestor(
    int distance)
  { 
    if (distance==0)
    { return this;
    }
    else if (parent!=null)
    { return parent.getAncestor(distance-1);
    }
    else
    { return null;
    }
  }

  @Override
  public int[] getPath()
  { return path;
  }

  @Override
  public int getPathIndex()
  { return path[path.length-1];
  }

  @Override
  public void setChild(
    int index,
    State child)
  { throw new UnsupportedOperationException("Cant set child of Iterationstate");
  }

  @Override
  public void setPathIndex(
    int index)
  { 
    throw new UnsupportedOperationException
      ("Cant set pathIndex of Iterationstate");
  }

  @Override
  public String getComponentId()
  { return id;
  }
  
  

}



