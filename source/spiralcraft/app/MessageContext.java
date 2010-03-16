//
// Copyright (c) 1998,2010 Michael Toth
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
package spiralcraft.app;

import java.util.LinkedList;


/**
 * <p>Routes Messages through a Component hierarchy optionally associated
 *   with a State tree.
 * </p>
 * 
 * @author mike
 */
public class MessageContext
{
  @SuppressWarnings("unused")
  private final MessageContext parent;

  
  private final boolean stateful;
  private String logPrefix;
  private StateFrame currentFrame;
  private LinkedList<Integer> path=new LinkedList<Integer>();
  private LinkedList<Integer> reversePath=new LinkedList<Integer>();
  private State state;
  private Component component;
  
  
  /**
   * <p>Create a GenerationContext that does not refer to any ancestors,
   *  and sends output to the specified Writer.
   * </p>
   * 
   * <p>If a StateFrame is not provided, a new one will be created
   * </p>
   */
  public MessageContext(boolean stateful,StateFrame frame)
  { 
    this.parent=null;
    this.stateful=stateful;
    this.currentFrame=frame;
    
    if (currentFrame==null && stateful)
    { currentFrame=new StateFrame();
    }
  }
  
  
  /**
   * Create a GenerationContext that refers to its ancestors for the resolution
   *   of dependencies.
   * 
   * @param parent The parent GenerationContext
   */
  public MessageContext(MessageContext parent)
  { 
    this.parent=parent;
    this.stateful=parent.isStateful();
    currentFrame=parent.getCurrentFrame();
  }

  
  
  /**
   * Provide a new StateFrame to trigger a State refresh
   * 
   * @param frame
   */
  public void setCurrentFrame(StateFrame frame)
  { this.currentFrame=frame;
  }
    
  
  /**
   * 
   * @return The state of the current Element, set by this Element's parent
   *   via setState()
   */
  public State getState()
  { return state;
  }

  /**
   * Send a Message into the Component hierarchy rooted at the 
   *   specified Component, optionally associated with
   *   the specified State, and routed to the Component at the specified
   *   path.
   * 
   * @param state
   */
  public void sendMessage
    (Message message
    ,Component component
    ,State state
    ,Integer[] path
    )
  { 
    State lastState=state;
    Component lastComponent=component;
    try
    {
      this.state=state;
      this.component=component;
      component.message(this,message);
      if (path!=null)
      {
        this.path=new LinkedList<Integer>();
        this.reversePath.clear();
        for (Integer val:path)
        { this.path.add(val);
        }
      }
    }
    finally
    { 
      this.state=lastState;
      this.component=lastComponent;
    }
  }
  
  
  /**
   * <p>A stateful rendering or messaging allows for direct
   *   manipulation of document content, but costs memory and CPU.
   * </p>
   * 
   * @return Whether ElementStates should be created and maintained for
   *   components.
   */
  public boolean isStateful()
  { return stateful;
  }
  
  public String getLogPrefix()
  { return logPrefix;
  }
  
  public StateFrame getCurrentFrame()
  { return currentFrame;
  }
  
  protected void setLogPrefix(String logPrefix)
  { this.logPrefix=logPrefix;
  }
  
  public final void handleEvent(Event event)
  {
    Component lastComponent=component;
    component=component.getParent();

    State lastState=state;
    
    int depth=component.getStateDepth();
    if (depth==1)
    { state=state.getParent();
    }
    else if (depth>1)
    { 
      for (;depth>0;depth--)
      { state=state.getParent();
      }
    }
    
    try
    { 
      if (component!=null)
      { component.asContainer().handleEvent(this,event);
      }
    }
    finally
    { 
      component=lastComponent;
      state=lastState;
    }
  }  
  
  
  /**
   * <p>Relay a message to the appropriate children of the current container
   *   as indicated by the message path.
   * </p>
   * 
   * @param message The message to relay
   *
   */
  public final void relayMessage(Message message)
  { 
    Container container=component.asContainer();
    
    if (container!=null)
    {
      if (path!=null && !path.isEmpty())
      {
        try
        { messageChild(pushPath(),message);
        }
        finally
        { popPath();
        }
      }
      else if (message.isMulticast())
      { 
        final int count=container.getChildCount();
        if (count>0)
        {
          for (int i=0;i<count;i++)
          { messageChild(i,message);
          }
        }
      }
    }
  }
  
  public final Integer pushPath()
  { 
    if (path!=null && !path.isEmpty())
    { 
      Integer element=path.removeFirst();
      reversePath.add(element);
      return element;
    }
    else
    { return null;
    }
  }
  
  public final void popPath()
  { path.add(reversePath.removeFirst());
  }
  
  public final void setIntermediateState(State state)
  { this.state=state;
  }
  
  /**
   * <p>Message a specific child of the current Container.
   * </p>
   * 
   * <p>This method ensures that the child Component's state is available in
   *   the messageContext, and ensures that the state of the current
   *   Container is restored to the messageContext upon return.
   * </p>
   * 
   * @param context
   * @param index
   */
  private void messageChild
    (int index
    ,Message message
    )
  { 
    if (isStateful() && state!=null)
    {
      final State lastState=state;
      final Component lastComponent=component;
      state=ensureChildState(index);
      try
      { 
        Component child=component.asContainer().getChild(index);
        child.message(this,message);
      }
      finally
      { 
        state=lastState;
        component=lastComponent;
      }
    }
    else
    { component.asContainer().getChild(index).message(this,message);
    }
  }  
  
  private State ensureChildState(int index)
  {
    State childState=state.getChild(index);
    if (childState==null)
    { 
      childState=component.asContainer().getChild(index).createState(state);
      state.setChild(index,childState);    
    }
    return childState;
  }
}