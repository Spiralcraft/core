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
package spiralcraft.app.kit;

import java.util.LinkedList;

import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Event;
import spiralcraft.app.Message;
import spiralcraft.app.Parent;
import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.util.Path;
import spiralcraft.util.Sequence;


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
public class StandardDispatcher
  implements Dispatcher
{
  // private final Dispatcher parent;

  
  private final boolean stateful;
  private String logPrefix;
  private StateFrame currentFrame;
  private LinkedList<Integer> path=new LinkedList<Integer>();
  private LinkedList<Integer> reversePath=new LinkedList<Integer>();
  private LinkedList<Integer> crumbtrail=new LinkedList<Integer>();
  
  private Path localPath;
  
  private State state;
  private Component component;
  
  public String toString()
  { return super.toString()+" path:"+path+"  reversePath:"+reversePath+"  component:"+component;
  }
  /**
   * <p>Create a Dispatcher that does not refer to any ancestors
   * </p>
   * 
   * <p>If a StateFrame is not provided, a new one will be created
   * </p>
   */
  public StandardDispatcher(boolean stateful,StateFrame frame)
  { 
    // this.parent=null;
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
  public StandardDispatcher(Dispatcher parent)
  { 
    // this.parent=parent;
    this.stateful=parent.isStateful();
    currentFrame=parent.getFrame();
  }

  
  
  /**
   * Provide a new StateFrame to trigger a State refresh
   * 
   * @param frame
   */
  public void setFrame(StateFrame frame)
  { this.currentFrame=frame;
  }
    
  
  /**
   * 
   * @return The state of the current Element, set by this Element's parent
   *   via setState()
   */
  @Override
  public State getState()
  { return state;
  }

  @Override
  public void dispatch(Message message,Sequence<Integer> path)
  { dispatch(message,component,state,path);
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
  @Override
  public void dispatch
    (Message message
    ,Component component
    ,State state
    ,Sequence<Integer> path
    )
  { 
    State lastState=state;
    Component lastComponent=component;
    LinkedList<Integer> lastCrumbtrail=crumbtrail;
    if (!message.isOutOfBand())
    { state.enterFrame(currentFrame);
    }
    
    try
    {
      this.state=state;
      this.component=component;
      this.crumbtrail=new LinkedList<Integer>();
      if (path!=null)
      {
        this.path=new LinkedList<Integer>();
        this.reversePath.clear();
        for (Integer val:path)
        { this.path.add(val);
        }
      }

      component.message(this,message);
    }
    finally
    { 
      if (!message.isOutOfBand())
      { state.exitFrame();
      }
      this.state=lastState;
      this.component=lastComponent;
      this.crumbtrail=lastCrumbtrail;
    }
  }
  
  
  /**
   * <p>A stateful model allows for interactivity, but costs memory and
   *   CPU
   * </p>
   * 
   * @return Whether States should be created and maintained for
   *   components.
   */
  @Override
  public boolean isStateful()
  { return stateful;
  }
  
  public String getLogPrefix()
  { return logPrefix;
  }
  
  @Override
  public StateFrame getFrame()
  { return currentFrame;
  }
  
  protected void setLogPrefix(String logPrefix)
  { this.logPrefix=logPrefix;
  }
  
  @Override
  public final void handleEvent(Event event)
  {
    // XXX This needs to use a Component stack, not the static component model
    
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

  @Override
  public final Integer getNextRoute()
  {  
    if (path!=null && !path.isEmpty())
    { return path.getFirst();
    }
    else
    { return null;
    }
  }
  
  @Override
  public final void descend(int index,boolean outOfBand)
  { 
    if (path!=null && !path.isEmpty())
    { 
      Integer element=path.removeFirst();
      if (element!=index)
      { 
        throw new RuntimeException
          ("Route violation: "+index+" != next segment "+element);
      }
      reversePath.add(element);
    }
    if (this.state!=null)
    { 
      this.state=this.state.getChild(index);
      if (!outOfBand)
      { this.state.enterFrame(currentFrame);
      }
    }
    this.crumbtrail.add(index);
  }
  
  @Override
  public final void ascend(boolean outOfBand)
  { 
    this.crumbtrail.removeLast();
    if (this.state!=null)
    { 
      if (!outOfBand)
      { this.state.exitFrame();
      }
      this.state=this.state.getParent();
    }
    if (reversePath!=null && !reversePath.isEmpty())
    { path.add(reversePath.removeFirst());
    }
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
   * Relay a message to a child component
   * 
   * @param childComponent
   * @param childIndex
   * @param message
   */
  @Override
  public void relayMessage
    (Component childComponent,int childIndex,Message message)
  {
    if (isStateful() && state!=null)
    {
      
      State childState=state.getChild(childIndex);
      if (childState==null)
      { 
        childState=childComponent.createState();
        state.setChild(childIndex,childState);    
      }
      
      final State lastState=this.state;
      final Component lastComponent=this.component;
      final Path lastLocalPath=this.localPath;
      
      descend(childIndex,message.isOutOfBand());
      try
      { 
        this.component=childComponent;
        childComponent.message(this,message);
      }
      finally
      {
        ascend(message.isOutOfBand());
        this.state=lastState;
        this.component=lastComponent;
        this.localPath=lastLocalPath;
      }
    }
  }


  @Override
  public void relayMessage
    (Component childComponent
    ,State newParentState
    ,int childIndex
    ,Message message
    )
  { 
    final State lastState=this.state;
    try
    { 
      this.state=newParentState;
      relayMessage(childComponent,childIndex,message);
    }
    finally
    { this.state=lastState;
    }
    
  }
  
  @Override
  public Sequence<Integer> getForwardPath()
  { 
    if (path.isEmpty())
    { return null;
    }
    else 
    { return new Sequence<Integer>(path.toArray(new Integer[path.size()]));
    }
  }
  
  @Override
  public boolean isTarget()
  { return path==null || path.isEmpty();
  }

  @Override
  public String getContextInfo()
  { return null;
  }
}
