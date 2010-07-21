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

import spiralcraft.util.Path;


/**
 * <p>Provides state and message context for an execution path through
 *   the component model
 * <p>
 * 
 * 
 * <p>Routes Messages through a Component hierarchy optionally associated
 *   with a State tree. 
 * </p>
 * 
 * <p>Scoped to a single thread
 * </p>
 * 
 * @author mike
 */
public class Dispatcher
{
  @SuppressWarnings("unused")
  private final Dispatcher parent;

  
  private final boolean stateful;
  private String logPrefix;
  private StateFrame currentFrame;
  private LinkedList<Integer> path=new LinkedList<Integer>();
  private LinkedList<Integer> reversePath=new LinkedList<Integer>();
  
  private Path localPath;
  
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
  public Dispatcher(boolean stateful,StateFrame frame)
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
  public Dispatcher(Dispatcher parent)
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
   * <p>Send a Message into the Component hierarchy rooted at the 
   *   specified Component, optionally associated with
   *   the specified State, and routed to the Component at the specified
   *   path.
   * </p>
   * 
   * <p>Called once at the root of the hierarchy to dispatch a message
   * </p>
   * 
   * @param state
   */
  public void dispatch
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
    component=component.getParent().asComponent();

    if (component!=null)
    {
      Parent parent=component.asParent();
      State lastState=state;
    
      int depth=parent.getStateDepth();
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
        { parent.handleEvent(this,event);
        }
      }
      finally
      { 
        component=lastComponent;
        state=lastState;
      }
    }
    else
    { component=lastComponent;
    }
  }  
  
  
  /**
   * <p>Relay a message to the appropriate children of the current container
   *   as indicated by the message path.
   * </p>
   * 
   * <p>This method is called by a Component once the pre-order stage of
   *   its local message processing has been completed. When this method
   *   returns, the Component can complete the post-order stage of
   *   its local message processing.
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
        // 
        try
        { messageChild(pushPath(),message);
        }
        finally
        { popPath();
        }
      }
      else if (message.isMulticast())
      { 
        // No specific child was specified, and message is multicast, so
        //   message all children
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
  
  public final Path getLocalPath()
  { return localPath;
  }
  
  /**
   * Specify an optional relative path for children to consume as
   *   a contextual address or state designator
   * 
   * @param localPath
   */
  public final void setLocalPath(Path localPath)
  { this.localPath=localPath;
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
      final Path lastLocalPath=localPath;
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
        localPath=lastLocalPath;
        
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
