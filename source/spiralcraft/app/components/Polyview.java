//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.components;


import java.util.HashMap;

import spiralcraft.app.Component;
import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.DynamicProxyComponent;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Reflector;
import spiralcraft.util.Sequence;

/**
 * Dynamically provides a view based on the actual data type of the model
 *   value.
 * 
 * @author mike
 *
 */
public class Polyview<T>
  extends AbstractComponent
{

  private Binding<T> x;
  private HashMap<Reflector<T>,Integer> polyMap
    = new HashMap<>();
  
  public void setX(Binding<T> x)
  { 
    this.removeParentContextual(this.x);
    this.x=x;
    this.addParentContextual(x);
  }

  protected Class<? extends State> getStateClass()
  { return PolyState.class;
  }
  
  // TODO: Override the bindInternal method to add a DispatchFilter that reads
  //  the mask in the state to select which component to dispatch to.
  
  protected void rotate(PolyState<T> state,T value)
  {
    Reflector<T> subtype=x.getReflector().subtype(value);
    if (subtype==null)
    { subtype=x.getReflector();
    }
    log.fine
      ("Subtype is "
        +subtype.getTypeURI()
      );
    
    if (subtype!=state.getActiveType())
    { 
      state.setActiveType(subtype);
      Integer childNum=polyMap.get(subtype);
      if (childNum==null)
      {
        Component child=new DynamicProxyComponent();
        child.setScaffold(this.scaffold);
        // TODO: Add childe to the appropriate childContainer
      }
      
      // TODO: Update the mask in the PolyState to control which component
      //  is active. This means that the polyMap returns an integer so we can
      //  be efficient about the selection.
    }
  }
  
}

/**
 * Keeps a substate for each discrete subtype. 
 * 
 * @author mike
 *
 * @param <T>
 */
class PolyState<T>
  implements State
{

  private Reflector<T> activeType;
  
  public PolyState(String id)
  { 
  }

  Reflector<T> getActiveType()
  { return activeType;
  }
  
  void setActiveType(Reflector<T> activeType)
  { this.activeType=activeType;
  }
  
  @Override
  public Sequence<Integer> getPath()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public State getParent()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public State getChild(
    int index)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void link(
    State parentState,
    Sequence<Integer> path)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setChild(
    int index,
    State child)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <X> X findState(
    Class<X> clazz)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public State getAncestor(
    int distance)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLocalId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void enterFrame(
    StateFrame frame)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void exitFrame()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public StateFrame getFrame()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isNewFrame()
  {
    // TODO Auto-generated method stub
    return false;
  }
}
