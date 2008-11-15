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
package spiralcraft.lang;

import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.lang.spi.SimpleChannel;

public abstract class BaseFocus<T>
  implements Focus<T>
{
  
  private volatile Channel<Focus<T>> selfChannel;
  
  protected Channel<T> subject;
  protected Focus<?> parent;
  protected NamespaceResolver namespaceResolver;
  private HashMap<Expression<?>,Channel<?>> channels; 


  public Focus<?> getParentFocus()
  { return parent;
  }
    
  public synchronized void setSubject(Channel<T> val)
  { 
    subject=val;
    channels=null;
  }
  
  
  public void setParentFocus(Focus<?> parent)
  { this.parent=parent;
  }
  
  /**
   * Return the Context for this Focus, or if there is none associated,
   *   return the Context for the parent Focus.
   */
  public Channel<?> getContext()
  { return subject;
  }
  
  
  /**
   * Return the subject of expression evaluation
   */
  public Channel<T> getSubject()
  { return subject;
  }
  
  public <Tchannel> TeleFocus<Tchannel> telescope(Channel<Tchannel> channel)
  { return new TeleFocus<Tchannel>(this,channel);
  }
  
  public <Tchannel> Focus<Tchannel> chain(Channel<Tchannel> channel)
  { return new SimpleFocus<Tchannel>(this,channel);
  }



  @SuppressWarnings("unchecked") // Heterogeneous hash map
  public synchronized <X> Channel<X> bind(Expression<X> expression)
    throws BindException
  { 
    if (expression==null)
    { throw new IllegalArgumentException("Expression cannot be null");
    }
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

  public NamespaceResolver getNamespaceResolver()
  { 
    if (namespaceResolver!=null)
    { return namespaceResolver;
    }
    else if (parent!=null)
    { return parent.getNamespaceResolver();
    }
    else
    { return null;
    }
  }
  
  public void setNamespaceResolver(NamespaceResolver resolver)
  { this.namespaceResolver=resolver;
  }
  
  @Override
  public LinkedList<Focus<?>> getFocusChain()
  {
    LinkedList<Focus<?>> list;
    if (parent==null)
    { 
      
      list=new LinkedList<Focus<?>>()
      {

        private static final long serialVersionUID = 1L;

        @Override
        public String toString()
        {
          StringBuilder buf=new StringBuilder();
          int i=0;
          for (Focus<?> focus : this)
          { buf.append("\r\n    focusChain #"+(i++)+": "+focus);
          }
          return buf.toString();
        }
      };
    }
    else
    { list=parent.getFocusChain();
    }
    list.push(this);
    return list;
    
  }
  
  public Channel<Focus<T>> getSelfChannel()
  { 
    if (selfChannel==null)
    {   
      try
      { selfChannel=new SimpleChannel<Focus<T>>(this,true);
      }
      catch (BindException x)
      { x.printStackTrace();
      }
    }
    return selfChannel;
  }
  
  @Override
  public String toString()
  { 
    return super.toString()
      +"["+(subject!=null
        ?(subject.getContentType().getName()
         +"-[@"+subject.getReflector().getTypeURI()+"] "
         +"("+subject.getClass().getName()+")"
         )
        :"")
      +"]";
  }


}
