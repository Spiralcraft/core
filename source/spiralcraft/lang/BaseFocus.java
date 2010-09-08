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

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.common.namespace.PrefixResolver;

import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.util.URIUtil;


public abstract class BaseFocus<T>
  implements Focus<T>
{
  
  private volatile Channel<Focus<T>> selfChannel;
  
  protected Channel<T> subject;
  protected final Focus<?> parent;
  protected PrefixResolver namespaceResolver;
  protected LinkedList<Focus<?>> facets;
  protected LinkedList<URI> aliases;
  protected boolean cacheChannels=false;

  private HashMap<Expression<?>,WeakReference<Channel<?>>> channels; 

  public BaseFocus()
  { this.parent=null;
  }
  
  public BaseFocus(Focus<?> parent)
  { this.parent=parent;
  }
  
  public BaseFocus(Focus<?> parent,Channel<T> subject)
  { 
    this.parent=parent;
    setSubject(subject);
  }
  

  @Override
  public Focus<?> getParentFocus()
  { return parent;
  }
    
  public synchronized void setSubject(Channel<T> val)
  { 
    subject=val;
    if (subject!=null && subject.getContext()==null)
    { subject.setContext(parent);
    }
    channels=null;
  }
  
  
//  public void setParentFocus(Focus<?> parent)
//  { 
//    this.parent=parent;
//    if (subject!=null && subject.getContext()==null)
//    { subject.setContext(parent);
//    }
//    
//  }
  
  /**
   * Return the Context for this Focus, or if there is none associated,
   *   return the Context for the parent Focus.
   */
  @Override
  public Channel<?> getContext()
  { return subject;
  }
  
  
  /**
   * Return the subject of expression evaluation
   */
  @Override
  public Channel<T> getSubject()
  { return subject;
  }
  
  @Override
  public <Tchannel> TeleFocus<Tchannel> telescope(Channel<Tchannel> channel)
  { return new TeleFocus<Tchannel>(this,channel);
  }
  
  @Override
  public <Tchannel> Focus<Tchannel> chain(Channel<Tchannel> channel)
  { return new SimpleFocus<Tchannel>(this,channel);
  }

  @Override
  public Focus<T> chain(PrefixResolver resolver)
  { return new NamespaceFocus<T>(this,resolver);
  }
  
  @Override
  public boolean isContext(Channel<?> channel)
  { return getSubject()==channel && getContext()==channel;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Heterogeneous hash map
  public synchronized <X> Channel<X> bind(Expression<X> expression)
    throws BindException
  { 
    if (expression==null)
    { throw new IllegalArgumentException("Expression cannot be null");
    }
    Channel<X> channel=null;
    
    if (cacheChannels)
    {
      if (channels==null)
      { channels=new HashMap<Expression<?>,WeakReference<Channel<?>>>();
      }
      else
      {  
        WeakReference<Channel<?>> ref=channels.get(expression);
        if (ref!=null)
        { channel=(Channel<X>) ref.get();
        }
      }
    
      if (channel==null)
      { 
        channel=expression.bind(this);
        channels.put(expression,new WeakReference(channel));
      }
    }
    else
    { channel=expression.bind(this);
    }
    
    if (channel.getContext()==null)
    { channel.setContext(this);
    }
    return channel;
  }

  @Override
  public PrefixResolver getNamespaceResolver()
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
  
  public void setNamespaceResolver(PrefixResolver resolver)
  { this.namespaceResolver=resolver;
  }
  
  
  @Override
  public boolean isFocus(URI uri)
  { 
    URI baseURI=uri.isAbsolute()?URIUtil.trimToPath(uri):null;
        
    if (subject==null)
    { return false;
    }
    
    if (aliases!=null)
    { 
      for (URI alias:aliases)
      {
        if (alias.equals(uri))
        { return true;
        }
      }
    } 

    if (baseURI!=null && subject.getReflector().isAssignableTo(baseURI))
    { return true;
    }
    
    return false;
  }  
  
  @SuppressWarnings("unchecked") // Cast for requested interface
  @Override
  public <X> Focus<X> findFocus(URI uri)
  { 
    if (isFocus(uri))
    { return (Focus<X>) this;
    }
    
    if (facets!=null)
    {
    
      for (Focus<?> focus:facets)
      {
        if (focus.isFocus(uri))
        { return (Focus<X>) focus;
        }
      }
    
    }
      
    if (parent!=null)
    { return parent.findFocus(uri);
    }
    else
    { return null;
    }
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
          { 
            buf.append("\r\n    focusChain #"+(i++)+": ");
            buf.append(focus.toFormattedString("\r\n    --"));  
            
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
  
  
  @Override
  public synchronized void addFacet(Focus<?> facet)
  { 
    if (facets==null)
    { facets=new LinkedList<Focus<?>>();
    }
    facets.add(facet);
  }
  
  @Override
  public synchronized void addAlias(URI alias)
  {
    if (aliases==null)
    { aliases=new LinkedList<URI>();
    }
    aliases.add(alias);
  }
  
 
  @Override
  public Channel<Focus<T>> getSelfChannel()
  { 
    if (selfChannel==null)
    { selfChannel=new SimpleChannel<Focus<T>>(this,true);
    }
    return selfChannel;
  }
  
  @Override
  public String toString()
  { 
    StringBuilder buf=new StringBuilder();
    buf.append(super.toString());
    buf.append("[");
    if (subject!=null)
    {
      if (subject.getReflector()!=null)
      { buf.append("[@"+subject.getReflector().getTypeURI()+"]");
      }
      if (subject.getContentType()!=null)
      { buf.append(":"+subject.getContentType().getName());
      }
      else
      { buf.append("(no content type!)");
      }
      buf.append(":("+subject.getClass().getName()+")");
    }
    else
    { buf.append("(no subject)");
    }
    buf.append("]");
    return buf.toString();
//    return super.toString()
//      +"["+(subject!=null
//        ?(subject.getContentType().getName()
//         +"-[@"+subject.getReflector().getTypeURI()+"] "
//         +"("+subject.getClass().getName()+")"
//         )
//        :"")
//      +"]";
  }

  @Override
  public String toFormattedString(String prefix)
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(prefix+toString());
    if (aliases!=null)
    { 
      for (URI uri:aliases)
      { buf.append(prefix).append("-- alias: "+uri);
      }
    }
    if (facets!=null)
    {
      for (Focus<?> focus:facets)
      { buf.append(focus.toFormattedString(prefix+"--"));
      }
    }
    return buf.toString();
  }

}
