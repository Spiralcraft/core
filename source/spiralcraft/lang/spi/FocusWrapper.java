//
// Copyright (c) 1998,2007 Michael Toth
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
import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.NamespaceResolver;
import spiralcraft.lang.TeleFocus;


/**
 * <p>Allows an application component to expose a delegated Focus to
 *   subcomponents while changing or restricting some aspect of its behavior.
 * </p>
 * 
 * <p>Examples include changing the namespace map (such as when expressions
 *   are being defined within another namespace context), or restricting the
 *   set of URIs accessible from findFocus()
 * </p>
 * 
 * @author mike
 *
 * @param <tFocus>
 * 
 * 
 */
public abstract class FocusWrapper<tFocus>
  implements Focus<tFocus>
{
  protected final Focus<tFocus> focus;
  private HashMap<Expression<?>,Channel<?>> channels;
  
  public FocusWrapper(Focus<tFocus> delegate)
  { this.focus=delegate;
  }
  
  
  /**
   * Bind is overridden to maintain wrapper
   */
  @Override
  @SuppressWarnings("unchecked") // Heterogeneous hash map
  public synchronized <X> Channel<X> bind(Expression<X> expression)
    throws BindException
  { 
    Channel<X> channel=null;
    if (channels==null)
    { channels=new HashMap<Expression<?>,Channel<?>>();
    }
    else
    { channel=(Channel<X>) channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      channels.put(expression,channel);
    }
    return channel;
  }


//  @Override
//  public <X> Channel<X> bind(
//    Expression<X> expression)
//    throws BindException
//  {  return focus.bind(expression);
//  }

  @Override
  public <X>Focus<X> findFocus(
    URI specifier)
  { return focus.<X>findFocus(specifier);
  }

  @Override
  public Channel<?> getContext()
  { return focus.getContext();
  }

  @Override
  public NamespaceResolver getNamespaceResolver()
  { return focus.getNamespaceResolver();
  }

  @Override
  public Focus<?> getParentFocus()
  { return focus.getParentFocus();
  }

  @Override
  public Channel<tFocus> getSubject()
  { return focus.getSubject();
  }

  @Override
  public boolean isFocus(
    URI specifier)
  { return focus.isFocus(specifier);
  }

  @Override
  public String toString()
  { return focus.toString();
  }
  
  public LinkedList<Focus<?>> getFocusChain() 
  { return focus.getFocusChain();
  }


  @Override
  public <Tchannel> Focus<Tchannel> chain(
    Channel<Tchannel> channel)
  { return focus.<Tchannel>chain(channel);
  }


  @Override
  public <Tchannel> TeleFocus<Tchannel> telescope(
    Channel<Tchannel> subject)
  { return focus.<Tchannel>telescope(subject);
  }
  
  
}
