//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.lang.spi;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BaseFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Provides a consistent snapshot of contextual Focus references over
 *   a single execution of a task. 
 * </p>
 * 
 * <p>Captures and caches the value of any Focus chain channel referenced from 
 *   down-chain components and child tasks for an operation demarcated by the 
 *   push() and pop() methods.
 * </p>
 * 
 * <p>Useful for making ThreadLocal context available to other Threads, for 
 *   enclosing context within behavioral elements, and for ensuring that
 *   expensive Channel access operations are not repeated excessively.
 * </p>
 * 
 * <p>When constructing the closure, a subject channel where isConstant()=true
 *   is considered already "closed" and will not be explicitly managed.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class ClosureFocus<T>
  extends BaseFocus<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(ClosureFocus.class);
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(ClosureFocus.class,null);
  
  @SuppressWarnings("rawtypes")
  private LinkedHashMap<URI,EnclosedFocus> foci
    =new LinkedHashMap<URI,EnclosedFocus>();
  private EnclosedFocus<T> subjectFocus;
  
  public ClosureFocus(Focus<T> focusChain)
  { this(focusChain,focusChain.getSubject());
  }
  
  public ClosureFocus(Focus<?> focusChain,Channel<T> subject)
  { 
    super(focusChain);

    if (subject!=null)
    {
      if (!subject.isConstant())
      {
        // Create an entry for the subject so we can enclose it as well
        subjectFocus=new EnclosedFocus<T>(subject);
        setSubject(subjectFocus.getSubject());
      }
      else
      { setSubject(subject);
      }
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <X> Focus<X> findFocus(URI focusURI)
  {
    if (isFocus(focusURI))
    { return (Focus<X>) this;
    }
    
    EnclosedFocus<X> focus=foci.get(focusURI);
    if (focus==null)
    {
      Focus<X> openFocus=super.<X>findFocus(focusURI);
      if (openFocus!=null)
      {
        Channel<X> subject=openFocus.getSubject();
        if (subject.isConstant())
        { return openFocus;
        }

// Use the enclose() method and the Closure object for inter-Thread
//   closure, so we don't have to create all our threads just-in-time.
//        
//        if (subject instanceof ThreadLocalChannel
//             && ((ThreadLocalChannel) subject).isInheritable()
//           )
//        { return openFocus;
//        }
        focus=new EnclosedFocus(subject);
        foci.put(focusURI,focus);
      }
    }
    return focus;
  }
  
  @SuppressWarnings({ "rawtypes" })
  /**
   * Caches the current data value of all the Channels obtained from
   *   ancestors for access by the current Thread.
   */
  public void push()
  {
    for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
    { entry.getValue().push();
    }
    if (subjectFocus!=null)
    { subjectFocus.push();
    }
  }
  
  /**
   * Releases the data values previously cached by push()
   *   for the current Thread.
   */
  @SuppressWarnings("rawtypes")
  public void pop()
  {
    if (subjectFocus!=null)
    { subjectFocus.pop();
    }
    for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
    { entry.getValue().pop();
    }
  }

  /**
   * Obtains a snapshot of the current data values of all the Channels
   *   obtained from ancestors.
   * 
   * @return
   */
  public Closure enclose()
  { return new Closure();
  } 
  
  /**
   * Obtains a snapshot of the current data values of all the Channels
   *   obtained from ancestors.
   * 
   * @return
   */
  public RecursionContext getRecursionContext(Focus<?> focusChain)
  { return new RecursionContext(focusChain);
  } 
  
  /**
   * Implements recursion in the FocusChain by re-binding URIs to the
   *   end of a recursive section of the chain and pushing the 
   *   
   * @author mike
   *
   */
  public class RecursionContext
  {
    
    private final Channel<?>[] downChannels;
                             
    @SuppressWarnings("rawtypes")
    public RecursionContext(Focus<?> focusChain)
    {
      int i=0;
      downChannels=new Channel<?>[foci.size()+(subjectFocus!=null?1:0)];
      for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
      { downChannels[i++]=focusChain.findFocus(entry.getKey()).getSubject();
      }
      if (subjectFocus!=null)
      { downChannels[i++]=focusChain.getSubject();
      }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void push()
    {
      int i=0;
      for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
      { entry.getValue().push(downChannels[i++].get());
      }       
      if (subjectFocus!=null)
      { ((EnclosedFocus) subjectFocus).push(downChannels[i++].get());
      }
    }
    
    @SuppressWarnings("rawtypes")
    public void pop()
    {
      if (subjectFocus!=null)
      { subjectFocus.pop();
      }
      for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
      { entry.getValue().pop();
      }       
    }

  }
  
  
  
  public class Closure
  {
    private final Object[] values;
    private final T subjectValue;
    
    @SuppressWarnings("rawtypes")
    Closure()
    {
      values=new Object[foci.size()];
      int i=0;
      for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
      { values[i++]=entry.getValue().pull();
      }       
      subjectValue=subjectFocus!=null?subjectFocus.pull():null;
    }
    
    /**
     * Provides access to the snapshot of data values for the current
     *   Thread. 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void push()
    {
      int i=0;
      for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
      { entry.getValue().push(values[i++]);
      }  
      if (subjectFocus!=null)
      { subjectFocus.push(subjectValue);
      }
    }
    
    /**
     * Releases the snapshot of data values for the current Thread.
     */
    @SuppressWarnings("rawtypes")
    public void pop()
    {
      if (subjectFocus!=null)
      { subjectFocus.pop();
      }
      for (Entry<URI, EnclosedFocus> entry : foci.entrySet())
      { entry.getValue().pop();
      }  
    }
  }
  
  class EnclosedFocus<Y>
    extends SimpleFocus<Y>
  { 
    private final Channel<Y> sourceChannel;
    
    EnclosedFocus(Channel<Y> channel)
    { 
      super(ClosureFocus.this,new ThreadLocalChannel<Y>(channel,true));
      sourceChannel=channel;
      if (debugLevel.canLog(Level.DEBUG))
      { log.debug("Enclosing "+channel.getReflector().getTypeURI());
      }
      
    }
    
    Y pull()
    { return sourceChannel.get();
    }
    
    void push()
    { 
      try
      {
        ((ThreadLocalChannel<Y>) getSubject()).push();
      }
      catch (AccessException x)
      { throw new AccessException("Error resolving value of "+sourceChannel,x);
      }
    }
    
    void push(Y val)
    { ((ThreadLocalChannel<Y>) getSubject()).push(val);
    }
    
    void pop()
    { ((ThreadLocalChannel<Y>) getSubject()).pop();
    }
    
  }
}
