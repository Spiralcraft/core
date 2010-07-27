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
package spiralcraft.app.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import spiralcraft.app.State;

public class ExpansionState<T>
  implements State
{

  private final State parent;
  private List<MementoState> children;
  private final int[] path;
  private final String componentId;
  private boolean valid;
  private final int grandchildCount;
  
  public ExpansionState(State parent,String componentId,int grandchildCount)
  { 
    this.parent=parent;
    if (parent!=null)
    { 
      int[] parentPath=parent.getPath();
      path=new int[parentPath.length+1];
      System.arraycopy(parentPath,0,path,0,parentPath.length);      
    }
    else
    { path = new int[0];
    }
    
    children=new ArrayList<MementoState>();
    this.componentId=componentId;
    this.grandchildCount=grandchildCount;
  }
  
  
  public void setPathIndex(int index)
  { path[path.length-1]=index;;
  }
  
  /**
   * 
   * @return The path from the root of the ComponentState tree 
   */
  public int[] getPath()
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
  
  /**
   * 
   * @return The index of this State within its parent State
   */
  public int getPathIndex()
  { return path[path.length-1];
  }
  
  public State getParent()
  { return parent;
  }
  
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
  
  class MementoState
    extends ValueState<T>
  {
    public MementoState(String key)
    { super(grandchildCount,ExpansionState.this,key);
    }
    
  }
}


