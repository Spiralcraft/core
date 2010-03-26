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
package spiralcraft.app.components;

import spiralcraft.app.InitializeMessage;
import spiralcraft.app.Message;
import spiralcraft.app.MessageContext;
import spiralcraft.app.State;
import spiralcraft.app.spi.AbstractComponent;
import spiralcraft.app.spi.ValueState;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.IterationDecorator;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;


import spiralcraft.util.LookaroundIterator;


/**
 * Iterate through a collection of some type
 */
@SuppressWarnings("unchecked") // Runtime type resolution
public class Iterate<T>
  extends AbstractComponent
{
  private static final ClassLog log=ClassLog.getInstance(Iterate.class);
  
  
  private Expression<?> expression;
  private Focus<?> currentFocus;
  private Focus<?> lookaheadFocus;
  private Focus<?> lookbehindFocus;

  private IterationDecorator<?,T> decorator;
  private ThreadLocalChannel valueChannel;
  private ThreadLocalChannel lookaheadChannel;
  private ThreadLocalChannel lookbehindChannel;
  
  private ThreadLocalChannel<Iteration> iterationLocal
    =new ThreadLocalChannel<Iteration>
      (BeanReflector.<Iteration>getInstance(Iteration.class));
  
  private boolean initializeContent;

  /**
   * The expression that references the Collection to iterate over
   * 
   * @param expression
   */
  public void setX(Expression<?> expression)
  { this.expression=expression;
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
  
  
  @Override
  public void message
    (final MessageContext context
    ,Message message
    )
  {
    Integer path=context.pushPath();
    try
    {

      IterationState state=getState(context);
      if (state==null || !state.isValid())
      { 
        if (message.getType()!=InitializeMessage.TYPE
            || initializeContent
            )
        { messageRefresh(context,message,path,state);
        }
      }
      else
      { messageRetraverse(context,message,path,state);
      }
    }
    finally
    { context.popPath();
    }
   
   
  }
  
  /**
   * <p>Message the component tree using the source iteration, creating or
   *   refreshing child states where necessary
   * </p>
   * 
   * @param context
   * @param message
   * @param path
   * @param state
   */
  private void messageRefresh
    (MessageContext context
   ,Message message
   ,Integer path
   ,IterationState state
   )
  { 
    Iteration iter=new Iteration();
    iterationLocal.push(iter);
    
    try
    {
      if (debugLevel.isDebug())
      { log.debug(toString()+": refreshing...");
      }      
      
      LookaroundIterator<T> cursor 
        = new LookaroundIterator(decorator.iterator());

      while (cursor.hasNext())
      { messageRefreshChild(context,message,cursor,iter,path,state);
      }
      state.trim(iter.index);
      state.setValid(true);

      if (debugLevel.isDebug())
      { log.debug(toString()+": refreshed "+iter.index+" elements");
      }      

    }
    finally
    { iterationLocal.pop();
    }
    
  }

  
  private void messageRefreshChild
    (MessageContext context
    ,Message message
    ,LookaroundIterator<T> cursor
    ,Iteration iter
    ,Integer path
    ,IterationState state
    )
  {
    T lastVal=cursor.getPrevious();
      
    T childVal=cursor.next();
    iter.hasNext=cursor.hasNext();
    
    ValueState<T> childState
      =(state!=null)
      ?state.ensureChild(iter.index,childVal)
      :null;
      
    if (path==null || path==iter.index)
    {
      pushElement(lastVal,childVal,cursor.getCurrent());
      try
      { 

      
        if (state!=null)
        {
          context.setIntermediateState
            (childState);
        }
        
        super.message(context,message);
      }
      finally
      { 
        if (state!=null)
        { context.setIntermediateState(state);
        }
        popElement();
      }
    }
    iter.index++;    
  }
  
  /**
   * <p>Retraverse the last iteration as represented in the IterationState
   * </p>
   * 
   * @param context
   * @param message
   * @param path
   */
  private void messageRetraverse
    (MessageContext context
    ,Message message
    ,Integer path
    ,IterationState state
    )
  { 
    Iteration iter=new Iteration();
    iterationLocal.push(iter);
    try
    {
      if (path!=null)
      {
      }
      else
      {
        if (debugLevel.isDebug())
        { log.debug(toString()+": retraversing...");
        }      
        LookaroundIterator<ValueState<T>> cursor 
          = new LookaroundIterator(state.iterator());

        while (cursor.hasNext())
        { messageRetraverseChild(context,message,cursor,iter,path);
        }
      
        if (debugLevel.isDebug())
        { log.debug(toString()+": retraversed "+iter.index+" elements");
        }
      }
    }
    finally
    { iterationLocal.pop();
    }
  }
  
  private void messageRetraverseChild
    (MessageContext context
    ,Message message
    ,LookaroundIterator<ValueState<T>> cursor
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
    if (path==null || path==iter.index)
    {
      pushElement
        (lastVal
        ,childState.getValue()
        ,cursor.getCurrent()!=null
          ?cursor.getCurrent().getValue()
          :null
        );
      
      IterationState state=getState(context);
      try
      {
        context.setIntermediateState(childState);
        
        // XXX May want to directy call context.relayMessage?
        super.message(context,message);
      }
      finally
      { 
        context.setIntermediateState(state);
        popElement();
      }
    }
    iter.index++;    
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
    
  
  @Override
  protected IterationState getState(MessageContext context)
  { return (IterationState) context.getState();
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> parentFocus)
    throws BindException
  { 
    Channel<?> target=null;
    if (expression!=null)
    { target=parentFocus.bind(expression);
    }
    else
    { 
      target=parentFocus.getSubject();
      if (target==null)
      { throw new BindException("Focus "+parentFocus+" has no subject");
      }
    }
  
    decorator=
      target.<IterationDecorator>decorate(IterationDecorator.class);
    
    if (decorator==null)
    { 
      throw new BindException
        ("Cannot iterate through a "+target.getContentType().getName());
    }
    return parentFocus;
  }

  @Override
  protected Focus<?> bindExports(Focus<?> parentFocus)
    throws BindException
  {
    
    
    {
      valueChannel
        =new ThreadLocalChannel(decorator.getComponentReflector());
    
      currentFocus=parentFocus.chain(valueChannel);
    
      currentFocus.addFacet
        (selfFocus);
    }
    
    {
      lookaheadChannel
        =new ThreadLocalChannel(decorator.getComponentReflector());

      lookaheadFocus=parentFocus.chain(lookaheadChannel);
      lookaheadFocus.addFacet
        (selfFocus);
      
    }
    
    {
      lookbehindChannel
        =new ThreadLocalChannel(decorator.getComponentReflector());
      lookbehindFocus=parentFocus.chain(lookbehindChannel);
      
      lookbehindFocus.addFacet(selfFocus);
      
    }
    
    if (debugLevel.isDebug())
    { log.debug("Iterator exposes "+valueChannel);
    }
    return currentFocus;
  }
    
  @Override
  public int getStateDepth()
  { return 2;
  }
  
  @Override
  public IterationState createState(State parent)
  { return new IterationState(container.getChildCount(),parent);
  }
  
  class Iteration
  {
    public int index;
    public boolean hasNext;
    
  }
  
}

