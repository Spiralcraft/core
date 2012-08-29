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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.util.Sequence;

public class ExpansionState<C,T>
  implements State
{

  private C collection;
  private List<MementoState> children;
  private final String localId;
  private final int grandchildCount;
  private boolean frameChanged;
  private StateFrame lastFrame;

  private boolean valid;
  private State parent;
  private Sequence<Integer> path;
  private HashMap<String,MementoState> childIdMap;
  
  public ExpansionState(int grandchildCount,String localId)
  {     
    children=new ArrayList<MementoState>();
    this.localId=localId;
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
  
  public C getCollection()
  { return collection;
  }
  
  void startRefresh(C collection)
  { 
    this.collection=collection;
    children.clear();
  }
  
  @Override
  public State getParent()
  { return parent;
  }
  
  @Override
  public State getChild(int index)
  { return children.get(index);
  }
  
  public MementoState findChild(String id)
  { return childIdMap!=null?childIdMap.get(id):null;
  }
  
  public void unmapChild(MementoState child)
  { 
    if (childIdMap!=null)
    { childIdMap.remove(child.getLocalId());
    }
  }
    
  public void trim(int index)
  { children=children.subList(0,index);
  }
    
  public MementoState createChild(int index,T childData,String childKey)
  { 
    MementoState ret=new MementoState(childKey);
    ret.setValue(childData);
    if (childIdMap==null)
    { childIdMap=new HashMap<String,MementoState>();
    }
    childIdMap.put(childKey,ret);
    moveChild(index,ret);
    return ret;
    
  }
  
  
  void moveChild(int index,MementoState child)
  {
    Sequence<Integer> newPath=path.concat(new Integer[index]);      
    child.link(this,newPath);
      
    int i=children.size()-index;
    while (i-->1)
    { children.add(null);
    }
    if (index<children.size())
    { children.set(index,child);
    }
    else
    { children.add(child);
    }
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
  public String getLocalId()
  { return localId;
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
  
  @Override
  public StateFrame getFrame()
  { return lastFrame;
  }
  
  List<MementoState> getChildren()
  { return children;
  }
  
  class MementoState
    extends ValueState<T>
  {
    public MementoState(String key)
    { super(grandchildCount,key);
    }
    
  }
}


