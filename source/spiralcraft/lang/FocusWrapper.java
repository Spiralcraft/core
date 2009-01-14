//
// Copyright (c) 2007,2009 Michael Toth
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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.common.NamespaceResolver;
import spiralcraft.lang.spi.SimpleChannel;


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
 * @param <Tfocus>
 * 
 * 
 */
public abstract class FocusWrapper<Tfocus>
  implements Focus<Tfocus>
{
  protected final Focus<Tfocus> focus;
  private HashMap<Expression<?>,Channel<?>> channels;
  private volatile Channel<Focus<Tfocus>> selfChannel;
  
  public FocusWrapper(Focus<Tfocus> delegate)
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
  public Channel<Tfocus> getSubject()
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


  public Channel<Focus<Tfocus>> getSelfChannel()
  {
    if (selfChannel==null)
    { selfChannel=new SimpleChannel<Focus<Tfocus>>(this,true);
    }
    return selfChannel;
  }
  
  @Override
  public <Tchannel> Focus<Tchannel> chain(Channel<Tchannel> channel)
  { return new SimpleFocus<Tchannel>(this,channel);
  }

  @Override
  public Focus<Tfocus> chain(NamespaceResolver resolver)
  { return new NamespaceFocus<Tfocus>(this,resolver);
  }

  @Override
  public <Tchannel> TeleFocus<Tchannel> telescope(
    Channel<Tchannel> subject)
  { return focus.<Tchannel>telescope(subject);
  }
  
  
}
