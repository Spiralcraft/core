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

import spiralcraft.app.CallMessage;
import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.Message;
import spiralcraft.app.Parent;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.LookaroundIterator;

public class ExpansionContainer<C,T>
  extends StandardContainer
{


  private static final ClassLog log
    =ClassLog.getInstance(ExpansionContainer.class);
  
  
  private Focus<T> currentFocus;
  protected Focus<T> lookaheadFocus;
  protected Focus<T> lookbehindFocus;

  private IterationDecorator<C,T> decorator;
  private ThreadLocalChannel<T> valueChannel;
  private ThreadLocalChannel<T> lookaheadChannel;
  private ThreadLocalChannel<T> lookbehindChannel;
  private Binding<?> keyBinding;
  
  private ThreadLocalChannel<Iteration> iterationLocal
    =new ThreadLocalChannel<Iteration>
      (BeanReflector.<Iteration>getInstance(Iteration.class));

  private boolean initializeContent;


  public ExpansionContainer(Parent parent)
  { super(parent);
  }
  
  public ExpansionContainer(Parent parent,Component[] children)
  { super(parent,children);
  }
  
  public void setKeyBinding(Binding<?> keyBinding)
  { this.keyBinding=keyBinding;
  }
  
  /**
   * 
   * @return The current index of the iteration being performed by the 
   *   current Thread
   */
  public int getIndex()
  { return iterationLocal.get().index;
  }
  
  /**
   * 
   * @return Whether the iteration being performed by the 
   *   current Thread is on the last element
   */
  public boolean isLast()
  { return !iterationLocal.get().hasNext;
  }
  
  /**
   * 
   * @return Whether the iteration being performed by the 
   *   current Thread is on the last element
   */
  public boolean isFirst()
  { return iterationLocal.get().index==0;
  }

  
//  public Focus<?> getLookaheadFocus()
//  { return lookaheadFocus;
//  }
//  
//  public Focus<?> getLookbehindFocus()
//  { return lookbehindFocus;
//  }

  /**
   * <p>Indicate whether the Initialize message should run the iteration and
   *   propogate the message to any content created as a result.
   * </p>
   * 
   * <p>The default value is false, to avoid the unnecessary retrieval of
   *   potentially expensive data.
   * </p>
   *   
   * @param val
   */
  public void setInitializeContent(boolean val)
  { initializeContent=val;
  }
  
  
  @SuppressWarnings("unchecked")
  @Override
  public void relayMessage
    (final Dispatcher dispatcher
    ,Message message
    )
  {
    if (logLevel.isFine())
    { log.fine("Relay message..."+message);
    }
    ExpansionState<T> state=(ExpansionState<T>) dispatcher.getState();

    Integer index=dispatcher.getNextRoute();
    if (state==null || !state.isValid())
    { 
      if (message.getType()!=InitializeMessage.TYPE
          || initializeContent
          )
      { messageRefresh(dispatcher,message,index,state);
      }
    }
    else
    { messageRetraverse(dispatcher,message,index,state);
    }
  }
  
  /**
   * <p>Message the component tree using the source iteration, creating or
   *   refreshing child states where necessary
   * </p>
   * 
   * @param dispatcher
   * @param message
   * @param path
   * @param state
   */
  private void messageRefresh
    (Dispatcher dispatcher
   ,Message message
   ,Integer path
   ,ExpansionState<T> state
   )
  { 
    Iteration iter=new Iteration();
    iterationLocal.push(iter);
    
    try
    {
      if (logLevel.isDebug())
      { log.debug(toString()+": refreshing...");
      }      
      
      LookaroundIterator<T> cursor 
        = new LookaroundIterator<T>(decorator.iterator());

      while (cursor.hasNext())
      { messageRefreshChild(dispatcher,message,cursor,iter,path,state);
      }
      state.trim(iter.index);
      state.setValid(true);

      if (logLevel.isDebug())
      { log.debug(toString()+": refreshed "+iter.index+" elements");
      }      

    }
    finally
    { iterationLocal.pop();
    }
    
  }

  
  private void messageRefreshChild
    (Dispatcher dispatcher
    ,Message message
    ,LookaroundIterator<T> cursor
    ,Iteration iter
    ,Integer path
    ,ExpansionState<T> state
    )
  {
    T lastVal=cursor.getPrevious();
      
    T childVal=cursor.next();
    iter.hasNext=cursor.hasNext();
    
    if (state!=null)
    { state.ensureChild(iter.index,childVal,childVal.toString());
    }
      
    if (path==null || path==iter.index)
    {
      pushElement(lastVal,childVal,cursor.getCurrent());
      dispatcher.descend(iter.index,message.isOutOfBand());
      try
      { messageChildren(dispatcher,message);
      }
      finally
      { 
        dispatcher.ascend(message.isOutOfBand());
        popElement();
      }
    }
    iter.index++;    
  }
  
  /**
   * <p>Retraverse the last iteration as represented in the ExpansionState
   * </p>
   * 
   * @param dispatcher
   * @param message
   * @param path
   */
  private void messageRetraverse
    (Dispatcher dispatcher
    ,Message message
    ,Integer path
    ,ExpansionState<T> state
    )
  { 
    Iteration iter=new Iteration();
    iterationLocal.push(iter);
    try
    {
      if (logLevel.isDebug())
      { log.debug(toString()+": retraversing...");
      }      
        
      LookaroundIterator<ExpansionState<T>.MementoState> cursor 
        = new LookaroundIterator<ExpansionState<T>.MementoState>
          (state.iterator());

      while (cursor.hasNext())
      { messageRetraverseChild(dispatcher,message,cursor,iter,path);
      }
      
      if (logLevel.isDebug())
      { log.debug(toString()+": retraversed "+iter.index+" elements");
      }
    }
    finally
    { iterationLocal.pop();
    }
  }
  
  private boolean isCallPath(Dispatcher dispatcher,int index)
  {
    if (callContext!=null)
    { 
      String key=callContext.getNextSegment();
      if (key!=null)
      { 
        if (keyBinding!=null)
        { return key.equals(keyBinding.get());
        }
        else
        { return key.equals(Integer.toString(index));
        }
      }
      else
      { 
        if (logLevel.isFine())
        { log.log(Level.FINE,"Path segment for call is null");
        }
      }
    }
    else
    { 
      if (logLevel.isFine())
      { log.log(Level.FINE,"No callContext for call");
      }
    }
    return false;
  }
  
  private void messageRetraverseChild
    (Dispatcher dispatcher
    ,Message message
    ,LookaroundIterator<ExpansionState<T>.MementoState> cursor
    ,Iteration iter
    ,Integer path
    )
  {
    T lastVal
      =cursor.getPrevious()!=null
      ?cursor.getPrevious().getValue()
      :null;
      
    ValueState<T> childState=cursor.next();
    iter.hasNext=cursor.hasNext();
    boolean callPath=false;
    if ( (path==null && message.isMulticast())
          || (path!=null && path==iter.index )
          || (message.getType()==CallMessage.TYPE
              && (callPath=isCallPath(dispatcher,iter.index))
             )
          || (subscribedTypes!=null 
               && subscribedTypes.contains(message.getType())
             )
         )
    {
      pushElement
        (lastVal
        ,childState.getValue()
        ,cursor.getCurrent()!=null
          ?cursor.getCurrent().getValue()
          :null
        );
      
      dispatcher.descend(iter.index,message.isOutOfBand());
      if (callPath)
      { callContext.descend();
      }
      try
      {
        // Run handlers for each element
        messageChildren(dispatcher,message);
      }
      finally
      { 
        if (callPath)
        { callContext.ascend();
        }
        dispatcher.ascend(message.isOutOfBand());
        popElement();
      }
    }
    iter.index++;    
  }
  
  protected void messageChild(
    Dispatcher dispatcher,
    int index,
    Message message)
  { dispatcher.relayMessage(children[index],index,message);
  }

  protected void messageChildren(
    Dispatcher dispatcher,
    Message message)
  {
    for (int index=0;index<children.length;index++)
    { dispatcher.relayMessage(children[index],index,message);
    }
  }
  
  private void pushElement(T lastVal,T val,T nextVal)
  {
    lookbehindChannel.push(lastVal);
    valueChannel.push(val);
    lookaheadChannel.push(nextVal);  
  }
  
  private void popElement()
  {
    lookaheadChannel.pop();
    valueChannel.pop();
    lookbehindChannel.pop();
  }
    
  
  


  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void bind(Focus<?> parentFocus)
    throws ContextualException
  {
    Channel<?> target=parentFocus.getSubject();
    if (target==null)
    { throw new BindException("Focus "+parentFocus+" has no subject");
    }
    
  
    decorator=
      target.<IterationDecorator>decorate(IterationDecorator.class);
    
    if (decorator==null)
    { 
      throw new BindException
        ("Cannot iterate through a "
          +target.getContentType().getName()
          +" ("+target.getReflector().getTypeURI()+")"
        );
    }
    
    {
      valueChannel
        =new ThreadLocalChannel<T>(decorator.getComponentReflector());
    
      currentFocus=parentFocus.chain(valueChannel);
      if (keyBinding!=null)
      { keyBinding.bind(currentFocus);
      }

    }
    
    {
      lookaheadChannel
        =new ThreadLocalChannel<T>(decorator.getComponentReflector());

      lookaheadFocus=parentFocus.chain(lookaheadChannel);

      
    }
    
    {
      lookbehindChannel
        =new ThreadLocalChannel<T>(decorator.getComponentReflector());
      lookbehindFocus=parentFocus.chain(lookbehindChannel);
      
      
    }
    
    if (logLevel.isDebug())
    { log.debug("Iterator exposes "+valueChannel);
    }
    super.bind(currentFocus);
  }
    
  class Iteration
  {
    public int index;
    public boolean hasNext;
    
  }
  
  
  
}
