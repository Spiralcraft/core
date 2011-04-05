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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.app.Event;
import spiralcraft.app.Parent;
import spiralcraft.app.Message;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.State;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;

/**
 * <p>Basic implementation of a component which uses a set of MessageHandlers
 *   to to handle incoming Messages.
 * </p>
 * 
 * @author mike
 *
 */
public class AbstractComponent
  implements Component,Parent
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level logLevel=ClassLog.getInitialDebugLevel(getClass(),null);
  protected Level normalLogLevel=logLevel;
  
  protected final MessageHandlerSupport handlers
    =new MessageHandlerSupport();
  
  protected Parent parent;
  protected boolean bound=false;
  protected Container childContainer;
  protected Focus<?> selfFocus;
  protected boolean acceptsChildren=true;

  protected boolean exportSelf=true;  
  protected String id;
  protected Object key;
  
  private Component[] peers;
  private HashSet<Component> peerSet;
  private Component[] contents;
  
  private LinkedList<Contextual> parentContextuals;
  private LinkedList<Contextual> exportContextuals;
  private LinkedList<Contextual> selfContextuals;
  
  public void setParent(Parent parent)
  { this.parent=parent;
  }
  
  @Override
  public Parent getParent()
  { return parent;
  }
  
  /**
   * A public identifier by which this component can be addressed within its
   *   parent component. Must be unique within the parent component.
   * 
   */
  public void setId(String id)
  { this.id=id;
  }
  
  /**
   * A public identifier by which this component can be addressed within its
   *   parent component.
   * 
   * @return
   */
  public String getId()
  { return this.id;
  }

//  /**
//   * An non-unique value that associates this component with some aspect of
//   *   its parent container.
//   * 
//   */
//  @Override
//  public void setKey(Object key)
//  { this.key=key;
//  }
//  
//  /**
//   * A non-unique value that associates this component with some aspect of
//   *   its parent container.
//   * 
//   * @return
//   */
//  @Override
//  public Object getKey()
//  { return this.key;
//  }

  
  /**
   * <p>Add a Peer Component
   * </p>
   * 
   * <p>A Peer is a child that has a specific role in this Component's function, 
   *   as opposed to Contents, which are generically managed children.
   * </p>
   * 
   * @param peer
   */
  protected void addPeer(Component peer)
  { 
    if (peer!=null)
    {
      if (this.peers==null)
      { 
        this.peers=new Component[] {peer};
        this.peerSet=new HashSet<Component>();
      }
      else 
      { this.peers=ArrayUtil.append(this.peers,peer);
      }
      this.peerSet.add(peer);
    }
  }
  
  /**
   * <p>Remove a Peer Component
   * </p>
   * 
   * <p>A Peer is a child that has a specific role in this Component's function, 
   *   as opposed to Contents, which are generically managed children.
   * </p>
   * 
   * @param peer
   */
  protected void removePeer(Component peer)
  {
    if (peer!=null)
    {
      if (this.peers!=null)
      { 
        this.peers=ArrayUtil.remove(this.peers,peer);
        this.peerSet.remove(peer);
      }
    }
  }
  
  /**
   * <p>Determine if a given child Component is a peer to exclude it from
   *   generic handling of child Components.
   * </p>
   * 
   * @param child
   * @return
   */
  protected boolean isPeer(Component child)
  { return peerSet!=null && peerSet.contains(child);
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's parent's context.
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   * 
   * @param contextual
   */
  protected void addParentContextual(Contextual contextual)
  { 
    if (this.parentContextuals==null)
    { this.parentContextuals=new LinkedList<Contextual>();
    }
    this.parentContextuals.add(contextual);
  }

  /**
   * <p>Remove a Contextual from the list of Contextuals to be bound
   * </p>
   * 
   * @param contextual
   */
  protected void removeParentContextual(Contextual contextual)
  {
    if (this.parentContextuals!=null)
    { this.parentContextuals.remove(contextual);
    }
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's target's context 
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   *
   * @param contextual
   */
  protected void addExportContextual(Contextual contextual)
  { 
    if (this.exportContextuals==null)
    { this.exportContextuals=new LinkedList<Contextual>();
    }
    this.exportContextuals.add(contextual);
  }

  /**
   * <p>Remove a Contextual from the list of Contextuals to be bound
   * </p>
   * 
   * @param contextual
   */
  protected void removeExportContextual(Contextual contextual)
  {
    if (this.exportContextuals!=null)
    { this.exportContextuals.remove(contextual);
    }
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's own context 
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   * 
   * @param contextual
   */
  protected void addSelfContextual(Contextual contextual)
  { 
    if (this.selfContextuals==null)
    { this.selfContextuals=new LinkedList<Contextual>();
    }
    this.selfContextuals.add(contextual);
  }
  
  
  /**
   * </p>Remove a Contextual from the list of Contextuals to be bound
   * <p>
   * 
   * @param contextual
   */
  protected void removeSelfContextual(Contextual contextual)
  {
    if (this.selfContextuals!=null)
    { this.selfContextuals.remove(contextual);
    }
  }
  
  protected final void bindContextuals
    (Focus<?> focus,List<Contextual> contextuals)
    throws BindException
  { 
    if (contextuals!=null)
    {
      for (Contextual contextual:contextuals)
      { contextual.bind(focus);
      }
    }
  }

  @Override
  public void message
    (Dispatcher context
    ,Message message
    )
  { 
    // Calls the context when done
    handlers.getChain(message.getType())
      .handleMessage(context,message);
  }

  @Override
  public Container asContainer()
  { return childContainer;
  }

  @Override
  public Parent asParent()
  { return this;
  }
  
  /**
   * <p>Override to create a new State.
   * </p>
   */
  @Override
  public State createState(State parentState)
  { return new SimpleState(-1,parentState,id);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    bound=true;
    bindContextuals(focusChain,parentContextuals);
    
    Focus<?> context=focusChain;
    if (selfFocus==null)
    { 
      selfFocus=focusChain.chain
        (new SimpleChannel<AbstractComponent>(this,true));
      bindContextuals(selfFocus,selfContextuals);
    }

    focusChain=bindImports(focusChain);
    
    focusChain=handlers.bind(focusChain);
    
    focusChain=bindExports(focusChain);
    if (exportSelf)
    {
      if (focusChain==context)
      { focusChain=selfFocus;
      }
      else
      { focusChain.addFacet(selfFocus);
      }
    }
    bindContextuals(focusChain,exportContextuals);

    List<Component> children=composeChildren(focusChain);
    if (children!=null)
    { 
      childContainer
        =createChildContainer(children.toArray(new Component[children.size()]));
    }
    
    if (childContainer!=null)
    { childContainer.bind(focusChain);
    }
    return focusChain;
  }

  /**
   * Assemble all child components in their appropriate order, starting with
   *   peers and any pre-defined contents.
   * 
   * @param focusChain
   */
  protected List<Component> composeChildren(Focus<?> focusChain)
  {
    if (peers==null && contents==null)
    { return null;
    }
    
    ArrayList<Component> childList
      =new ArrayList<Component>
        ( (peers!=null?peers.length:0) + (contents!=null?contents.length:0));
    if (peers!=null)
    { 
      for (Component comp:peers)
      { childList.add(comp);
      }
    }
    
    if (contents!=null)
    { 
      for (Component comp:contents)
      { childList.add(comp);
      }
    }
    
    return childList;
    
  }
  
  /**
   * Create the container for child components. Defaults to creating a
   *   StandardContainer.
   * 
   * @param children
   * @return
   */
  protected Container createChildContainer(Component[] children)
  { return new StandardContainer(children);
  }
  
  
  @Override
  public void start()
    throws LifecycleException
  { 
    Lifecycler.start
      (new Lifecycle[] 
        {handlers,childContainer}
      );
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    Lifecycler.stop
      (new Lifecycle[]
        {handlers,childContainer}
      );
  }

  protected Focus<?> bindImports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }

  protected Focus<?> bindExports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }
  
  @Override
  public int getStateDepth()
  { return 1;
  }

  protected State getState(Dispatcher context)
  { return context.getState();
  }
  
  @Override
  public int getStateDistance(Class<?> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return getStateDepth()-1;
    }
    else if (parent!=null)
    { 
      int parentDist=parent.getStateDistance(clazz);
      if (parentDist>-1)
      { return parentDist+getStateDepth();
      }
      else
      { return -1;
      }
    }
    else
    { return -1;
    }
  }

  @Override
  public Component asComponent()
  { return this;
  }

  protected void setContents(final Component[] contents)
  {
    if (!acceptsChildren)
    { 
      throw new UnsupportedOperationException
        (getClass()+" does not accept children");
    }
    this.contents=contents;
  }
    
  public boolean isBound()
  { return bound;
  }
  
  @Override
  public void handleEvent(
    Dispatcher context,
    Event event)
  { context.handleEvent(event);    
  }

  @Override
  public void setLogLevel(Level logLevel)
  { 
    this.logLevel=logLevel;
    this.normalLogLevel=logLevel;
  }

  public void setDebug(boolean debug)
  { 
    if (debug)
    { this.logLevel=Level.FINE;
    }
    else
    { 
      if (this.logLevel.canLog(Level.DEBUG))
      { this.logLevel=this.normalLogLevel;
      }
    }
  }
}
