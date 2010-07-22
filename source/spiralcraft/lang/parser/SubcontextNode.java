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
package spiralcraft.lang.parser;

import java.util.ArrayList;
import java.util.List;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.FocusChannel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;


/**
 * <p>Represents an Expression bound against a subcontext derived from another 
 *   Expression via a telescoped Focus-
 * </p> 
 *   
 * <p>For example:
 * </p>
 * 
 * <code>expr1{ expr2 }</code>
 *   
 * @author mike
 *
 * @param <T> The projection (return element) type
 * @param <S> The source type
 */
public class SubcontextNode<T,S>
  extends Node
{

  private final Node _source;
  private final List<Node> _subcontextList;

  public SubcontextNode(Node source,List<Node> subcontextList)
  { 
    _source=source;
    _subcontextList=subcontextList;
  }

  @Override
  public Node[] getSources()
  { 
    ArrayList<Node> ret=new ArrayList<Node>();
    ret.add(_source);
    ret.addAll(_subcontextList);
    return ret.toArray(new Node[ret.size()]);
  }  

  @Override
  public Node copy(Object visitor)
  {
    boolean dirty=false;
    List<Node> nodes=new ArrayList<Node>();
    for (Node node:_subcontextList)
    { 
      Node nodeCopy=node.copy(visitor);
      nodes.add(nodeCopy);
      if (node!=nodeCopy)
      { dirty=true;
      }
    }
    SubcontextNode<T,S> copy
      =new SubcontextNode<T,S>(_source.copy(visitor),nodes);
    if (!dirty && copy._source==_source )
    { return this;
    }
    else
    { return copy;
    }
  }  

  
  @Override
  public String reconstruct()
  { 
    StringBuilder ret=new StringBuilder();
    ret.append(_source.reconstruct()+" { ");
    boolean first=true;
    for (Node node : _subcontextList)
    {
      if (first)
      { first=false;
      }
      else
      { ret.append(" , ");
      }
      ret.append(node.reconstruct());
    }
    ret.append(" } ");
    return ret.toString();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
   
    Channel<S> sourceChannel=focus.<S>bind(new Expression<S>(_source,null));
    
    if (sourceChannel instanceof FocusChannel)
    { focus=((FocusChannel) sourceChannel).getFocus();
    }
    
    Channel<?>[] channels=new Channel<?>[_subcontextList.size()];
    
    if (sourceChannel.isConstant())
    {
      Focus<S> subFocus=focus.telescope(sourceChannel);
      int i=0;
      for (Node node:_subcontextList)
      { channels[i++]=subFocus.bind(new Expression(node));
      }
      return new SubcontextChannel(sourceChannel,null,channels);
      
    }
    else
    {
    
      ThreadLocalChannel<S> sourceLocal
        =new ThreadLocalChannel<S>(sourceChannel,true);
    
      Focus<S> subFocus=focus.telescope(sourceLocal);
      int i=0;
      for (Node node:_subcontextList)
      { channels[i++]=subFocus.bind(new Expression(node));
      }
      return new SubcontextChannel(sourceChannel,sourceLocal,channels);
    }
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Subcontext ");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
    out.append(prefix+"{");
    boolean first=true;
    for (Node node : _subcontextList)
    {
      if (first)
      { first=false;
      }
      else
      { out.append(" , ");
      }
      node.dumpTree(out,prefix+"  ");
    }
    out.append(prefix+"}");
  }
  

}

class SubcontextChannel<T,S>
  extends SourcedChannel<S,T>
{
  private final  Channel<?>[] channels;
  private final ThreadLocalChannel<S> sourceLocal;
  
  
  @SuppressWarnings("unchecked")
  SubcontextChannel
    (Channel<S> source
    ,ThreadLocalChannel<S> sourceLocal
    ,Channel[] subcontext
    )
    throws BindException
  {
    super(subcontext[subcontext.length-1].getReflector(),source);
    
    this.channels=subcontext;
    this.sourceLocal=sourceLocal;

  }

  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  {
    if (sourceLocal!=null)
    { sourceLocal.push(source.get());
    }
    try
    {
      Object ret=null;
    
      for (Channel<?> channel : channels)
      { ret=channel.get();
      }
      return (T) ret;
    }
    finally
    { 
      if (sourceLocal!=null)
      { sourceLocal.pop();
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected boolean store(
    T val)
    throws AccessException
  { 
    if (sourceLocal!=null)
    { sourceLocal.push();
    }
    try
    { return ((Channel<T>) channels[channels.length-1]).set(val);
    }
    finally
    {
      if (sourceLocal!=null)
      { sourceLocal.pop();
      }
    }
  }
}

