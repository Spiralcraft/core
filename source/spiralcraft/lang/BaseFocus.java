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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.common.namespace.PrefixResolver;

import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.util.URIUtil;


public abstract class BaseFocus<T>
  implements Focus<T>
{
  private static final ClassLog log=ClassLog.getInstance(BaseFocus.class);
  
  private volatile Channel<Focus<T>> selfChannel;
  
  protected Channel<T> subject;
  protected final Focus<?> parent;
  protected PrefixResolver namespaceResolver;
  protected LinkedList<Focus<?>> facets;
  protected LinkedList<URI> aliases;

  private final HashMap<URI,Focus<?>> findCache=new HashMap<URI,Focus<?>>();

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
  public synchronized <X> Channel<X> bind(Expression<X> expression)
    throws BindException
  { 
    if (expression==null)
    { throw new IllegalArgumentException("Expression cannot be null");
    }
    Channel<X> channel=null;
    

    channel=expression.bind(this);
    
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
    URI baseURI=URIUtil.trimToPath(uri);
        
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
    
    if (baseURI!=null 
        && subject!=null
        && subject.isConstant() 
        && Declarable.class.isAssignableFrom(subject.getContentType())
        )
    { 
      DeclarationInfo info=((Declarable) subject.get()).getDeclarationInfo();
      if (info!=null && info.instanceOf(baseURI))
      { return true;
      }
    }
    
    return false;
  }  
  
  private synchronized Focus<?> findCached(URI uri)
  { return findCache.get(uri);
  }
  
  private synchronized Focus<?> putCache(URI uri,Focus<?> focus)
  { 
    findCache.put(uri, focus);
    return focus;
  }
  
  @SuppressWarnings("unchecked") // Cast for requested interface
  @Override
  public <X> Focus<X> findFocus(URI uri)
  { 
    Focus<?> cached=findCached(uri);
    if (cached!=null)
    { return (Focus<X>) cached;
    }
    
    
    
    if (isFocus(uri))
    { return (Focus<X>) putCache(uri,this);
    }
    
    if (facets!=null)
    {
    
      for (Focus<?> focus:facets)
      {
        if (focus.isFocus(uri))
        { return (Focus<X>)  putCache(uri,focus);
        }
      }
    
    }
      
    if (parent!=null)
    { return (Focus<X>) putCache(uri,parent.findFocus(uri));
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
    if (!findCache.isEmpty())
    { 
      URI maskedURI=null;
      for (URI uri:findCache.keySet())
      {
        if (facet.isFocus(uri) 
            && findCache.get(uri).getSubject()
                != facet.getSubject()
            )
        { maskedURI=uri;
        }
      }
      if (maskedURI!=null)
      { 
        log.info("Facet added to focus conflicts with cached result for "
                  +maskedURI+": "+this+" facet="+facet
                );
        // XXX We may change some behavior if we
        //   do this- wait for analysis of effects
        //findCache.clear();        
      }
    }
  }
  
  @Override
  public synchronized void addAlias(URI alias)
  {
    if (alias==null)
    { throw new IllegalArgumentException("Cannot add null alias to Focus");
    }
    if (aliases==null)
    { aliases=new LinkedList<URI>();
    }
    else if (aliases.contains(alias))
    { return;
    }
    aliases.add(alias);
  }
  
 
  @Override
  public Channel<Focus<T>> getSelfChannel()
  { 
    if (selfChannel==null)
    { selfChannel=LangUtil.constantChannel((Focus<T>) this);
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
