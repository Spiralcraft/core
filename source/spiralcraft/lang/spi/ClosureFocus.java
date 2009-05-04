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

import spiralcraft.lang.BaseFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.log.ClassLog;

/**
 * <p>Provides a consistent snapshot of contextual data for an operation.
 * </p>
 * 
 * <p>Captures and caches the current content of all Channels referenced from 
 *   down-chain components for a period of time demarcated by the push() and
 *   pop() methods.
 * </p>
 * 
 * <p>Useful for making ThreadLocal context available to other Threads, for 
 *   enclosing context within behavioral elements, and for ensuring that
 *   expensive Channel access operations are not repeated excessively.
 * </p>
 * 
 * <p>When constructing the closure, a subject channel that is already
 *   an inheritable ThreadLocalChannel, or where isConstant()=true is
 *   considered already "closed" and will not be explicitly managed.
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
  
  private LinkedHashMap<URI,EnclosedFocus<?>> foci
    =new LinkedHashMap<URI,EnclosedFocus<?>>();
  
  public ClosureFocus(Focus<?> focusChain)
  { parent=focusChain;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <X> Focus<X> findFocus(URI focusURI)
  {
    EnclosedFocus<X> focus=(EnclosedFocus<X>) foci.get(focusURI);
    if (focus==null)
    {
      Focus<X> openFocus=parent.<X>findFocus(focusURI);
      if (openFocus!=null)
      {
        Channel<X> subject=openFocus.getSubject();
        if (subject.isConstant())
        { return openFocus;
        }
        if (subject instanceof ThreadLocalChannel
             && ((ThreadLocalChannel) subject).isInheritable()
           )
        { return openFocus;
        }
        focus=new EnclosedFocus(subject);
        foci.put(focusURI,focus);
      }
    }
    return focus;
  }

  public boolean isFocus(URI focusURI)
  { return false;
  }


  public void push()
  {
    for (Entry<URI, EnclosedFocus<?>> entry : foci.entrySet())
    { entry.getValue().push();
    }
  }
  
  public void pop()
  {
    for (Entry<URI, EnclosedFocus<?>> entry : foci.entrySet())
    { entry.getValue().pop();
    }
  }

  
  class EnclosedFocus<Y>
    extends SimpleFocus<Y>
  { 
    private final Channel<Y> sourceChannel;
    
    public EnclosedFocus(Channel<Y> channel)
    { 
      super(new ThreadLocalChannel<Y>(channel.getReflector(),true));
      sourceChannel=channel;
      parent=ClosureFocus.this;
      log.debug("Enclosing "+channel.getReflector().getTypeURI());
    }
    
    public void push()
    { ((ThreadLocalChannel<Y>) getSubject()).push(sourceChannel.get());
    }
    
    public void pop()
    { ((ThreadLocalChannel<Y>) getSubject()).pop();
    }
    
  }
}
