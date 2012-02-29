//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app.kit;

import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.util.Sequence;

public class SimpleState
  implements State
{

  private State parent;
  private final State[] children;
  private Sequence<Integer> path;
  private final String componentId;
  private boolean frameChanged;
  private StateFrame lastFrame;
  
  public SimpleState(int childCount,String componentId)
  {     
    if (childCount>0)
    { children=new State[childCount];
    }
    else
    { children=null;
    }
    this.componentId=componentId;
    
  }
  
  
//  @Override
//  public void setPathIndex(int index)
//  { path[path.length-1]=index;;
//  }
//  
  
  @Override
  public void link(State parent,Sequence<Integer> path)
  { 
    if (this.parent!=null)
    { throw new IllegalStateException("Can't change parent from "+this.parent);
    }
    this.parent=parent;
    this.path=path;
  }
  
  /**
   * 
   * @return The path from the root of the ComponentState tree 
   */
  @Override
  public Sequence<Integer> getPath()
  { return path;
  }
  

//  
//  /**
//   * 
//   * @return The index of this ElementState within its parent ElementState
//   */
//  @Override
//  public int getPathIndex()
//  { return path[path.length-1];
//  }
  
  @Override
  public State getParent()
  { return parent;
  }
  
  @Override
  public State getChild(int index)
  { 
    if (children==null)
    { throw new IndexOutOfBoundsException("This State has no children");
    }
    else
    { return children[index];
    }
  }
    
  
  @Override
  public void setChild(int index,State child)
  { 
    children[index]=child;
    Sequence<Integer> newPath;
    if (path!=null)
    { newPath=path.concat(new Integer[] {index});
    }
    else
    { newPath=new Sequence<Integer>(new Integer[] {index});
    }
    child.link(this,newPath);
  }
  
  /**
   * Find an ElementState among this ElementState's ancestors/containers
   * 
   * @param <X>
   * @param clazz
   * @return The ElementState with the specific class or interface, or null if
   *   none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  @Override
  public <X> X findState(Class<X> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findState(clazz);
    }
    else
    { return null;
    }
  }
  
  /**
   * <p>Return an ancestor of this state that is the specified
   *   number of parents away.
   * </p>
   * 
   * @param distance The number of states to traverse, where 0 indicates
   *   that this state should be returned and 1 indicates that this state's
   *   parent should be returned.
   * @return The ancestor state.
   */
  @Override
  public State getAncestor(int distance)
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
  public String getComponentId()
  { return componentId;
  }
  
  @Override
  public void enterFrame(StateFrame frame)
  {
    if (lastFrame!=frame)
    { 
      lastFrame=frame;
      frameChanged=true;
    }
    
  }
  
  @Override
  public void exitFrame()
  { frameChanged=false;
  }
    
  @Override
  public boolean isNewFrame()
  { return frameChanged;
  }
}


