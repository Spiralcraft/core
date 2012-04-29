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
package spiralcraft.app.kit;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.app.Pipe;
import spiralcraft.app.Event;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.Parent;
import spiralcraft.app.Message;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.State;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.lang.BindException;
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Context;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Basic implementation of a component which uses a set of MessageHandlers
 *   to to handle incoming Messages.
 * </p>
 * 
 * @author mike
 *
 */
public class AbstractComponent
  implements Component,Parent,Declarable
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level logLevel=ClassLog.getInitialDebugLevel(getClass(),Level.INFO);
  protected Level normalLogLevel=logLevel;
  
  protected MessageHandlerChain handler;
  protected Constructor<? extends State> stateConstructor;
  
  private MessageHandler[] messageHandlers;
  private Context[] outerContexts;
  
  protected Parent parent;
  protected boolean bound=false;
  protected Container childContainer;
  protected Focus<?> selfFocus;
  protected boolean acceptsChildren=true;

  protected String id;
  protected Object key;
  
  private PeerSet peerSet;
  
  private Component[] contents;
  
  private LinkedList<Contextual> parentContextuals;
  private LinkedList<Contextual> exportContextuals;
  private LinkedList<Contextual> selfContextuals;
  
  private ChainableContext outerContext;
  
  private HashSet<Message.Type> subscriptions;
  private HashSet<Message.Type> notifications;
  private DeclarationInfo declarationInfo;
  
  @Override
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
  @Override
  public String getId()
  { return this.id;
  }

  /**
   * <p>Message handlers provided via explicit configuration
   * </p>
   * 
   * @return
   */
  public MessageHandler[] getMessageHandlers()
  { return messageHandlers;
  }
  
  /**
   * <p>Provide additional message handlers to this Component
   * </p>
   * 
   * @return
   */
  public void setMessageHandlers(MessageHandler[] messageHandlers)
  { this.messageHandlers=messageHandlers;
  }

  /**
   * <p>Specify a set of Contexts thataugment the environment in which
   *   this component and its children operate.
   * </p>
   * @param contexts
   */
  public void setContext(Context[] contexts)
  { this.outerContexts=contexts;
  }
  
    
  @Override
  public Message.Type[] getSubscribedTypes()
  { 
    if (subscriptions!=null)
    { return subscriptions.toArray(new Message.Type[subscriptions.size()]);
    }
    else
    { return null;
    }
  }
  
  /**
   * @see Parent.subscribe()
   */
  @Override
  public void subscribe(Message.Type[] types)
  {    
    for (Message.Type type : types)
    { 
      // Don't create a subscription when this component generates 
      //   the notifications.
      if (notifications==null || !notifications.contains(type))
      { 
        if (subscriptions==null)
        { subscriptions=new HashSet<Message.Type>();
        }  
        
        subscriptions.add(type);
      }
    }
  }
  
  /**
   * Internal subscribe
   * 
   * @param types
   */
  protected void subscribe(Set<Message.Type> types)
  {
    if (types==null)
    { return;
    }
    for (Message.Type type : types)
    { 
      // Don't create a subscription when this component generates 
      //   the notifications.
      if (notifications==null || !notifications.contains(type))
      { 
        if (subscriptions==null)
        { subscriptions=new HashSet<Message.Type>();
        }  
        
        subscriptions.add(type);
      }
    }
  }
  
  /**
   * Dispatch a new message to all children that subscribe to the message
   *   type.
   * 
   * @param myState
   * @param message
   */
  protected final void notify(State myState,Message message)
  { 
    new StandardDispatcher
      (true,myState.getFrame()).dispatch(message,this,myState,null);
    
  }
  
  /**
   * Register a message type that this component's children can subscribe to
   * 
   * @param publishedType
   */
  protected void addPublishedType(Message.Type publishedType)
  {
    if (notifications==null)
    { notifications=new HashSet<Message.Type>();
    }
    notifications.add(publishedType);
  }
  
  protected MessageHandler createDefaultHandler()
  { return new DefaultHandler();
  }
    
  protected synchronized void addHandler(MessageHandler handler)
  { 
    if (this.handler==null)
    { this.handler=new StandardMessageHandlerChain(handler);
    }
    else
    { this.handler.chain(handler);
    }
  }
  
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
      if (this.peerSet==null)
      { 
        this.peerSet=new PeerSet();
      }
      this.peerSet.add(peer);
    }
  }
  
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
  protected void addPeer(Peering peering)
  { 
    if (peering!=null)
    {
      if (this.peerSet==null)
      { 
        this.peerSet=new PeerSet();
      }
      this.peerSet.add(peering);
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
      if (this.peerSet!=null)
      { this.peerSet.remove(peer);

      }
    }
  }
  
  protected Peering getPeering(Component peer)
  {
    if (peer!=null)
    {
      if (this.peerSet!=null)
      { return this.peerSet.getPeering(peer);
      }
    }
    return null;
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
  
  protected void chainOuterContext(Context context)
  {
    if (outerContext==null)
    { outerContext=AbstractChainableContext.createChain(context);
    }
    else
    { outerContext.chain(context);
    }
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
    throws ContextualException
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
    (Dispatcher dispatcher
    ,Message message
    )
  { 
    try
    {
      
      if (logLevel.isFine())
      { log.fine("entering: "+message);
      }
      if (outerContext!=null)
      { outerContext.push();
      }
      try
      {
        if (handler!=null)
        { handler.handleMessage(dispatcher,message);
        }
        else if (childContainer!=null)
        { childContainer.relayMessage(dispatcher,message);
        }
      }
      finally
      {
        if (outerContext!=null)
        { outerContext.pop();
        }
      }
      if (logLevel.isFine())
      { log.fine("exiting: "+message);
      }

    }
    catch (RuntimeException x)
    { throw new RuntimeException
        ("Messaging "+getClass()+" #"+getId()+" "
          +dispatcher.getState().getPath()
        ,x
        );
    }
    
  }

  protected Container asContainer()
  { return childContainer;
  }
  
  protected Component[] getChildren()
  { 
    if (childContainer!=null)
    { return childContainer.getChildren();
    }
    else
    { return null;
    }
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
  public State createState()
  { 
    if (stateConstructor!=null)
    { 
      try
      {
        return stateConstructor.newInstance
          ( new Object[] 
              {childContainer!=null?childContainer.getChildCount():-1,id} 
          );
      }
      catch (Exception x)
      { 
        throw new RuntimeException
          ("Error creating state "+getStateClass(),x);
      }
    }
    else
    { 
      return new SimpleState
        (childContainer!=null?childContainer.getChildCount():-1,id);
    }
  }

  @Override
  public final Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    bound=true;
    if (parent==this)
    { throw new IllegalStateException("Recursive parent");
    }
    
    Contextual self=new Contextual()
    {
      @Override
      public Focus<?> bind(Focus<?> focusChain)
        throws ContextualException
      { return bindInternal(focusChain);
      }
    };
    
    if (outerContexts!=null)
    { 
      for (Context context:outerContexts)
      { chainOuterContext(context);
      }
    }
    
    if (outerContext!=null)
    { 
      outerContext.seal(self);
      return outerContext.bind(focusChain);
    }
    else
    { return self.bind(focusChain);
    }
    
  }
  
  protected Class<? extends State> getStateClass()
  { return SimpleState.class;
  }
  
  protected void bindComplete(Focus<?> focusChain)
    throws ContextualException
  {
  }

  private final Focus<?> bindInternal(Focus<?> focusChain)
    throws ContextualException
  {
    bindContextuals(focusChain,parentContextuals);

    try
    {
      stateConstructor
        =getStateClass().getConstructor
          (new Class<?>[] {Integer.TYPE,String.class});
    }
    catch (NoSuchMethodException x)
    {  
    }    
    
    Focus<?> parentContext=focusChain;
    if (selfFocus==null)
    { 
      selfFocus=focusChain.chain
        (new SimpleChannel<AbstractComponent>(AbstractComponent.this,true));
      bindContextuals(selfFocus,selfContextuals);
      if (peerSet!=null)
      { peerSet.bind(selfFocus);
      }
    }

    focusChain=bindImports(focusChain);
    
    
    
    if (focusChain==parentContext)
    { focusChain=focusChain.chain(focusChain.getSubject());
    }
    focusChain.addFacet(selfFocus);
    
    
    addHandlers();
    
    if (messageHandlers!=null)
    { 
      for (MessageHandler messageHandler:messageHandlers)
      { addHandler(messageHandler);
      }
    }
    
    if (handler!=null)
    {
      handler.chain(createDefaultHandler());
      handler.bind(focusChain);
    }
    
    focusChain=bindExports(focusChain);
    
    bindContextuals(focusChain,exportContextuals);

    List<Component> children=composeChildren(focusChain);
    if (children!=null)
    { 

      childContainer
        =createChildContainer(children.toArray(new Component[children.size()]));
    }
      
    if (childContainer!=null)
    { 
      childContainer.bind(focusChain);
      subscribe(childContainer.getSubscribedTypes());
    }
    
    bindComplete(focusChain);
    
    return bindReturn(focusChain);
  }
  
  /**
   * Override to return something other than selfFocus to this Component's
   *   container
   *   
   * @param focus
   * @return
   */
  protected Focus<?> bindReturn(Focus<?> focus)
  { return selfFocus;
  }
  
  /**
   * Assemble all child components in their appropriate order, starting with
   *   peers and any pre-defined contents.
   * 
   * @param focusChain
   */
  protected List<Component> composeChildren(Focus<?> focusChain)
  {
    if (peerSet==null && contents==null)
    { return null;
    }
    
    ArrayList<Component> childList
      =new ArrayList<Component>
        ( (peerSet!=null?peerSet.size():0) + (contents!=null?contents.length:0));
    if (peerSet!=null)
    { 
      for (Component comp:peerSet)
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
  { 
    StandardContainer container=new StandardContainer(this,children);
    container.setLog(log);
    container.setLogLevel(logLevel);
    return container;
  }
  
  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (childContainer!=null)
    { childContainer.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (childContainer!=null)
    { childContainer.stop();
    }
  }

  protected Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }

  /**
   * <p>Override to add any additional handlers to the handler chain after
   *   all configuration properties have been supplied and imports have been
   *   bound.
   * </p>
   */
  protected void addHandlers()
  { 
  }
  
  protected Focus<?> bindExports(Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }
  
  @Override
  public int getStateDepth()
  { return 1;
  }

  protected State getState(Dispatcher dispatcher)
  { return dispatcher.getState();
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
  
  /**
   * Find a Component among this Component's ancestors/containers
   * 
   * @param <X>
   * @param clazz
   * @return The Element with the specific class or interface, or null if
   *   none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  @Override
  public <X> X findComponent(Class<X> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findComponent(clazz);
    }
    else
    { return null;
    }
  }
  
  /**
   * Find a Component among this Component's ancestors/containers, but stop
   *   looking when we reach the stop class.
   * 
   * @param <X>
   * @param clazz
   * @return The Element with the specific class or interface, or null if
   *   none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  @Override
  public <X> X findComponent(Class<X> clazz,Class<?> stop)
  {
    if (stop.isAssignableFrom(getClass()))
    { return null;
    }
    else if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findComponent(clazz,stop);
    }
    else
    { return null;
    }
    
  }
  
  public Component findAncestralChildOf(Component ancestor)
  {
    Parent parent=this;
    
    while (parent!=null)
    {
      if (parent.asComponent()!=null)
      { 
        Parent grandparent=parent.asComponent().getParent();
        if (grandparent==ancestor)
        { return parent.asComponent();
        }
        else
        { parent=grandparent;
        }
      }
      else
      { parent=null;
      }
    }
    return null;
    
  }
  
  protected int getChildCount()
  { return childContainer!=null?childContainer.getChildCount():0;
  }
  
  /**
   * Get a Pipe to the specified state in this Component's state tree
   * 
   * @param state
   * @return
   */
  protected Pipe getPipe(State state)
  {
    Component root=this;
    Parent parent=this;
    while (parent!=null)
    { 
      parent=root.getParent();
      if (parent!=null)
      { 
        Component component=parent.asComponent();
        if (component!=null)
        { root=component;
        }
      }
    }
    
    State rootState=state;
    while (rootState.getParent()!=null)
    { rootState=rootState.getParent();
    }
    
    return new StandardPipe(root,rootState,state.getPath());
  }
    
  class DefaultHandler
    implements MessageHandler
  {

    @Override
    public Focus<?> bind(Focus<?> focusChain)
      throws BindException
    { return focusChain;
    }

    @Override
    public void handleMessage(
      Dispatcher dispatcher,
      Message message,
      MessageHandlerChain chain)
    { 
      
      if (childContainer!=null)
      { childContainer.relayMessage(dispatcher,message);
      }
    }

  }

  @Override
  public void setDeclarationInfo(
    DeclarationInfo declarationInfo)
  { this.declarationInfo=declarationInfo;
  }

  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
  }
}
