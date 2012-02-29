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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.util.Sequence;

public class ExpansionState<T>
  implements State
{

  private List<MementoState> children;
  private final String componentId;
  private final int grandchildCount;
  private boolean frameChanged;
  private StateFrame lastFrame;

  private boolean valid;
  private State parent;
  private Sequence<Integer> path;
  
  public ExpansionState(String componentId,int grandchildCount)
  {     
    children=new ArrayList<MementoState>();
    this.componentId=componentId;
    this.grandchildCount=grandchildCount;
  }
  
  
  @Override
  public void link(State parent,Sequence<Integer> path)
  { 
    if (this.parent!=null)
    { throw new IllegalStateException("Can't change parent from "+this.parent);
    }
    this.parent=parent;
    this.path=path;
  }
   
//  @Override
//  public void setPathIndex(int index)
//  { path[path.length-1]=index;;
//  }
  
  /**
   * 
   * @return The path from the root of the ComponentState tree 
   */
  @Override
  public Sequence<Integer> getPath()
  { return path;
  }
  

  public void invalidate()
  { this.valid=false;
  }
  
  public void setValid(boolean valid)
  { this.valid=valid;
  }
  
  public boolean isValid()
  { return valid;
  }
  
//  /**
//   * 
//   * @return The index of this State within its parent State
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
  { return children.get(index);
  }
  
  public void trim(int index)
  { children=children.subList(0,index);
  }
    
  public MementoState ensureChild(int index,T childData,String childKey)
  { 
    MementoState ret=null;
    if (index<children.size())
    { ret=children.get(index);
    }
    
    if (ret==null)
    {
      ret=new MementoState(childKey);
      
      Sequence<Integer> newPath=path.concat(new Integer[index]);      
      ret.link(this,newPath);
      
      
      ret.setValue(childData);
      int i=children.size()-index;
      while (i-->1)
      { children.add(null);
      }
      if (index<children.size())
      { children.set(index,ret);
      }
      else
      { children.add(ret);
      }
    }
    
    return ret;
    
  }
  
    
  
  @Override
  public void setChild(int index,State child)
  { 
    throw new UnsupportedOperationException
      ("Cannot alter children of an ExpansionState");
  }

  
  public Iterator<MementoState> iterator()
  { return children.iterator();
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
    { lastFrame=frame;
    }
    frameChanged=true;
  }
  
  @Override
  public void exitFrame()
  { frameChanged=false;
  }
    
  @Override
  public boolean isNewFrame()
  { return frameChanged;
  }
  class MementoState
    extends ValueState<T>
  {
    public MementoState(String key)
    { super(grandchildCount,key);
    }
    
  }
}


