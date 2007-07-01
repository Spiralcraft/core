//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Simple implementation of Focus
 */
public class DefaultFocus<T>
  implements Focus<T>
{

  private Channel<?> _context;
  private Channel<T> _subject;
  private Focus<?> _parent;
  private HashMap<Expression<?>,Channel<?>> _channels;
  private HashSet<String> names;

  public DefaultFocus()
  {
  }

  public DefaultFocus(Channel<T> subject)
  { _subject=subject;
  }

  public void setParentFocus(Focus parent)
  { _parent=parent;
  }

  public Focus<?> getParentFocus()
  { return _parent;
  }

  public void setContext(Channel val)
  { _context=val;
  }
    
  public synchronized void setSubject(Channel<T> val)
  { 
    _subject=val;
    _channels=null;
  }

  /**
   * Specify that this Focus is addressed by the <CODE>[<I>name</I>]</CODE>
   *   operator.
   * 
   * @param name
   */
  public synchronized void addNames(String ... newNames)
  { 
    if (newNames!=null)
    {
      if (names==null)
      { names=new HashSet<String>();
      }
      for (String name: newNames)
      { names.add(name);
      }
    }
  }
  
  
  /**
   * Return the Context for this Focus, or if there is none associated,
   *   return the Context for the parent Focus.
   */
  public Channel<?> getContext()
  { 
    if (_context!=null)
    { return _context;
    }
    else if (_parent!=null)
    { return _parent.getContext();
    }
    else
    { return null;
    }
    
  }

  /**
   * Return the subject of expression evaluation
   */
  public Channel<T> getSubject()
  { return _subject;
  }

  public Focus<?> findFocus(String name)
  { 
    if (names!=null && names.contains(name))
    { return this;
    }
    if (_parent!=null)
    { return _parent.findFocus(name);
    }
    else
    { return null;
    }
  }

  @SuppressWarnings("unchecked") // Heterogeneous hash map
  public synchronized <X> Channel<X> bind(Expression<X> expression)
    throws BindException
  { 
    Channel<X> channel=null;
    if (_channels==null)
    { _channels=new HashMap<Expression<?>,Channel<?>>();
    }
    else
    { channel=(Channel<X>) _channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      _channels.put(expression,channel);
    }
    return channel;
  }
}
