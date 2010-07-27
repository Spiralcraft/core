//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.app.spi;



import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.State;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;

public class StandardContainer
  implements Container
{

  public StandardContainer()
  {
  }
  
  public StandardContainer(Component[] children)
  { this.children=children;
  }
  
  protected Component[] children;
  protected Level logLevel=Level.INFO;

  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    resolveChildren(focusChain);
    bindChildren(focusChain);
    return focusChain;
  }
  
  /**
   * Override to generate/decorate children based on the exported data
   *   type
   * 
   * @param focusChain
   */
  protected final void resolveChildren(Focus<?> focusChain)
  {
  }
  
  protected final void bindChildren(Focus<?> focusChain)
    throws BindException
  { 
    for (Component child:children)
    { child.bind(focusChain);
    }
  }
  
 

  @Override
  public Component getChild(
    int childNum)
  { return children[childNum];
  }


  @Override
  public int getChildCount()
  { return children.length;
  }


  @Override
  public Component[] getChildren()
  { return children;
  }
  



  @Override
  public void start()
    throws LifecycleException
  {
    for (Component child:children)
    { 
      child.start();
      
    }
    
  }


  @Override
  public void stop()
    throws LifecycleException
  {
    for (Component child:children)
    { 
      
      child.stop();
      
    }
    
  }



  protected State ensureStaticChildState(State parentState,int stateIndex)
  {
    State childState=parentState.getChild(stateIndex);
    if (childState==null)
    { 
      childState=children[stateIndex].createState(parentState);
      parentState.setChild(stateIndex,childState);    
    }
    return childState;
  }

  @Override
  public void messageChild(
    Dispatcher dispatcher,
    int index,
    Message message)
  {
    dispatcher
      .setNextState(ensureStaticChildState(dispatcher.getState(),index));
    children[index].message(dispatcher,message);
    
  }

  @Override
  public void messageChildren(
    Dispatcher dispatcher,
    Message message)
  {
    int index=0;
    for (Component child:children)
    {
      dispatcher
        .setNextState(ensureStaticChildState(dispatcher.getState(),index++));
      child.message(dispatcher,message);    
    }
    // TODO Auto-generated method stub
    
  }
}
