//
// Copyright (c) 2026 Michael Toth
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
package spiralcraft.lang.functions;

import java.util.Iterator;
import java.util.NoSuchElementException;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.reflect.IterableReflector;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>An iterable defined by a step expression: the walk's source, then the
 *   node the step returns from it, and so on, one next node for each step.
 * </p>
 *
 * <p>The walk ends when the step returns null, when while returns false
 *   for the next node, or throws when it passes maxSteps. A node is
 *   yielded when filter returns true for it, as out's value when out is
 *   given, as the node itself otherwise.
 * </p>
 *
 * <p>Each traversal (each iterator) has its own WalkCursor, published as
 *   [WalkCursor] to step, while, filter and out, which are bound against a
 *   focus whose subject is the current node. state is evaluated once when
 *   a traversal starts, with the source as its subject, and is the
 *   cursor's state.
 * </p>
 *
 * <p>The walk knows that a node is the last by taking the step from it
 *   before the node is yielded: the step runs one node ahead of the
 *   consumer.
 * </p>
 *
 * <p>The result is an Iterable, so every consumer (a projection, a
 *   subscript, .@top) gets an IterationDecorator from its Reflector, whose
 *   component Reflector is that of the yielded elements.
 * </p>
 *
 * @param <T> The type of the nodes
 * @param <R> The type of the elements yielded
 */
public class Walk<T,R>
  implements ChannelFactory<Iterable<R>,T>
{

  private Expression<T> stepX;
  private Expression<Boolean> filterX;
  private Expression<Boolean> whileX;
  private Expression<?> stateX;
  private Expression<R> outX;
  private int maxSteps=Integer.MAX_VALUE;

  public Walk()
  {
  }

  public Walk(Expression<T> stepX)
  { this.stepX=stepX;
  }

  /**
   * <p>The step: from the current node, the next node, or null to end
   *   the walk.
   * </p>
   */
  public void setStep(Expression<T> stepX)
  { this.stepX=stepX;
  }

  /**
   * <p>Whether the current node is yielded, evaluated after while.
   * </p>
   */
  public void setFilter(Expression<Boolean> filterX)
  { this.filterX=filterX;
  }

  /**
   * <p>Whether the walk goes on to the current node: evaluated for each
   *   node, the source included, before it is yielded; the walk ends,
   *   without yielding the node, when it returns false.
   * </p>
   */
  public void setWhile(Expression<Boolean> whileX)
  { this.whileX=whileX;
  }

  /**
   * <p>The traversal's state, evaluated once when a traversal starts, with
   *   the source as its subject; [WalkCursor].state, typed as this
   *   expression is.
   * </p>
   */
  public void setState(Expression<?> stateX)
  { this.stateX=stateX;
  }

  /**
   * <p>The element yielded for the current node, in place of the node.
   * </p>
   */
  public void setOut(Expression<R> outX)
  { this.outX=outX;
  }

  /**
   * <p>A failsafe: the walk throws when it reaches a node past this many,
   *   the source counted. Integer.MAX_VALUE when unset.
   * </p>
   */
  public void setMaxSteps(int maxSteps)
  { this.maxSteps=maxSteps;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<Iterable<R>> bindChannel
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?>[] arguments
    )
    throws BindException
  {
    if (stepX==null)
    { throw new BindException("fn:Walk requires a step expression");
    }

    Reflector<T> nodeReflector=source.getReflector();
    ThreadLocalChannel<T> itemChannel
      =new ThreadLocalChannel<T>(nodeReflector);

    Channel<?> stateChannel
      =stateX!=null
      ?focus.telescope(itemChannel).bind(stateX)
      :null;

    GenericReflector<WalkCursor> cursorReflector
      =new GenericReflector<WalkCursor>
        (BeanReflector.<WalkCursor>getInstance(WalkCursor.class));
    cursorReflector.enhance("value",nodeReflector);
    if (stateChannel!=null)
    { cursorReflector.enhance("state",stateChannel.getReflector());
    }
    ThreadLocalChannel<WalkCursor> cursorChannel
      =new ThreadLocalChannel<WalkCursor>(cursorReflector);

    Focus<T> walkFocus=focus.telescope(itemChannel);
    walkFocus.addFacet(new SimpleFocus<WalkCursor>(cursorChannel));

    Channel<T> stepChannel=walkFocus.bind(stepX);
    if (!nodeReflector.isAssignableFrom(stepChannel.getReflector()))
    {
      throw new BindException
        ("fn:Walk step returns "+stepChannel.getReflector().getTypeURI()
        +", not assignable to its source "+nodeReflector.getTypeURI()
        );
    }

    Channel<Boolean> whileChannel
      =whileX!=null?walkFocus.bind(whileX):null;
    Channel<Boolean> filterChannel
      =filterX!=null?walkFocus.bind(filterX):null;
    Channel<R> outChannel
      =outX!=null?walkFocus.bind(outX):null;

    Reflector<R> elementReflector
      =outChannel!=null
      ?outChannel.getReflector()
      :(Reflector<R>) nodeReflector;

    return new WalkChannel
      (source
      ,IterableReflector.getInstance(elementReflector)
      ,itemChannel
      ,cursorChannel
      ,stateChannel
      ,stepChannel
      ,whileChannel
      ,filterChannel
      ,outChannel
      );
  }

  /**
   * <p>Provides a new Iterable over the walk from the source's current
   *   value each time it is retrieved; each of its iterators is one
   *   traversal.
   * </p>
   */
  @SuppressWarnings("rawtypes")
  class WalkChannel
    extends SourcedChannel<T,Iterable<R>>
  {
    private final ThreadLocalChannel<T> itemChannel;
    private final ThreadLocalChannel<WalkCursor> cursorChannel;
    private final Channel<?> stateChannel;
    private final Channel<T> stepChannel;
    private final Channel<Boolean> whileChannel;
    private final Channel<Boolean> filterChannel;
    private final Channel<R> outChannel;

    WalkChannel
      (Channel<T> source
      ,Reflector<Iterable<R>> reflector
      ,ThreadLocalChannel<T> itemChannel
      ,ThreadLocalChannel<WalkCursor> cursorChannel
      ,Channel<?> stateChannel
      ,Channel<T> stepChannel
      ,Channel<Boolean> whileChannel
      ,Channel<Boolean> filterChannel
      ,Channel<R> outChannel
      )
    {
      super(reflector,source);
      this.itemChannel=itemChannel;
      this.cursorChannel=cursorChannel;
      this.stateChannel=stateChannel;
      this.stepChannel=stepChannel;
      this.whileChannel=whileChannel;
      this.filterChannel=filterChannel;
      this.outChannel=outChannel;
    }

    @Override
    public boolean isWritable()
    { return false;
    }

    @Override
    protected Iterable<R> retrieve()
    {
      final T start=source.get();
      return new Iterable<R>()
      {
        @Override
        public Iterator<R> iterator()
        { return new WalkIterator(start);
        }
      };
    }

    @Override
    protected boolean store(Iterable<R> val)
      throws AccessException
    { return false;
    }

    /**
     * <p>One traversal: its cursor, the next node not yet stepped from, and
     *   the next element found to yield.
     * </p>
     */
    class WalkIterator
      implements Iterator<R>
    {
      private final WalkCursor<T,Object> cursor;
      private T node;
      private int index;
      private R pending;
      private boolean hasPending;

      @SuppressWarnings("unchecked")
      WalkIterator(T start)
      {
        Object state
          =stateChannel!=null?evaluate(stateChannel,start,null):null;
        cursor=new WalkCursor<T,Object>(state);
        node=admit(start,0)?start:null;
      }

      @Override
      public boolean hasNext()
      {
        if (!hasPending)
        { advance();
        }
        return hasPending;
      }

      @Override
      public R next()
      {
        if (!hasNext())
        { throw new NoSuchElementException();
        }
        R ret=pending;
        pending=null;
        hasPending=false;
        return ret;
      }

      @Override
      public void remove()
      { throw new UnsupportedOperationException();
      }

      /**
       * <p>Step from each node in turn until one is yielded or the walk
       *   ends.
       * </p>
       */
      private void advance()
      {
        while (node!=null)
        {
          T current=node;
          int i=index;

          cursor.position(i,current,false);
          T next=evaluate(stepChannel,current,cursor);
          boolean goesOn=admit(next,i+1);

          node=goesOn?next:null;
          index=i+1;

          cursor.position(i,current,!goesOn);
          if (filterChannel==null
              || Boolean.TRUE.equals(evaluate(filterChannel,current,cursor))
             )
          {
            pending
              =outChannel!=null
              ?evaluate(outChannel,current,cursor)
              :uncheckedElement(current);
            hasPending=true;
            return;
          }
        }
      }

      /**
       * <p>Whether the walk goes on to a node: it is not null, it is within
       *   maxSteps, and while holds for it.
       * </p>
       */
      private boolean admit(T candidate,int i)
      {
        if (candidate==null)
        { return false;
        }
        if (i>=maxSteps)
        {
          throw new AccessException
            ("fn:Walk passed maxSteps ("+maxSteps+") at "+candidate);
        }
        if (whileChannel==null)
        { return true;
        }
        cursor.position(i,candidate,false);
        return Boolean.TRUE.equals(evaluate(whileChannel,candidate,cursor));
      }

      @SuppressWarnings("unchecked")
      private R uncheckedElement(T node)
      { return (R) node;
      }

      /**
       * <p>Evaluate one of the walk's expressions with a node as its
       *   subject and this traversal's cursor published.
       * </p>
       */
      @SuppressWarnings("unchecked")
      private <X> X evaluate(Channel<X> channel,T item,WalkCursor c)
      {
        itemChannel.push(item);
        cursorChannel.push(c);
        try
        { return channel.get();
        }
        finally
        {
          cursorChannel.pop();
          itemChannel.pop();
        }
      }
    }
  }
}
