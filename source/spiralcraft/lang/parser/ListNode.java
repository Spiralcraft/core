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
package spiralcraft.lang.parser;

import java.util.ArrayList;
import java.util.List;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.reflect.CollectionReflector;
import spiralcraft.lang.spi.AbstractChannel;

public class ListNode<T>
  extends Node
{

  private final List<Node> sources;
  
  public ListNode(List<Node> sources)
  { this.sources=sources;
  }

  @Override
  public Node[] getSources()
  { return sources.toArray(new Node[sources.size()]);
  }
  
  @Override
  public Node copy(Object visitor)
  {
    boolean dirty=false;
    ArrayList<Node> copy=new ArrayList<Node>(sources.size());
    for (Node node:sources)
    {
      Node itemCopy=node.copy(visitor);
      copy.add(itemCopy);
      if (itemCopy!=node)
      { dirty=true;
      }
    }
    if (dirty)
    { return new ListNode<T>(copy);
    }
    else
    { return this;
    }
  }
  
  @Override
  public String reconstruct()
  { 
    StringBuilder builder=new StringBuilder();
    builder.append(" { ");
    boolean first=true;
    for (Node node:sources)
    { 
      if (first)
      { first=false;
      }
      else
      { builder.append(" , ");
      }
      builder.append(node.reconstruct());
    }
    builder.append(" } ");
    return builder.toString();
  }
  

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
    final Channel<T>[] channels=new Channel[sources.size()];
    
    Reflector<T> componentReflector=null;
    int i=0;
    // Negotiate what "T" is 
    for (Node node:sources)
    { 
      Channel<T> channel=(Channel<T>) node.bind(focus);
      if (componentReflector==null)
      { componentReflector=channel.getReflector();
      }
      else
      { 
        Reflector<?> commonReflector
          =componentReflector.getCommonType(channel.getReflector());
        if (commonReflector==null)
        { 
          throw new BindException
            (componentReflector.getTypeURI()
            +" has nothing in common with "
            +channel.getReflector().getTypeURI()
            );
        }
        componentReflector=(Reflector<T>) commonReflector;
        		
          
      }
      channels[i++]=channel;
    }
    
    Reflector<List<T>> reflector
      =componentReflector!=null
      ?CollectionReflector.<List<T>,T>getInstance(List.class,componentReflector)
      :BeanReflector.<List<T>>getInstance(List.class)
      ;
    if (componentReflector==null)
    { componentReflector=BeanReflector.getInstance(List.class);
    }
    
    return new AbstractChannel<List<T>>
      (reflector)
    {

      { this.context=focus;
      }
      
      @Override
      protected List<T> retrieve()
      {
        List<T> list=new ArrayList(channels.length);
        for (Channel<T> channel: channels)
        { list.add(channel.get());
        }
        return list;
      }

      @Override
      protected boolean store(
        List<T> val)
        throws AccessException
      {
        if (val==null)
        {
          for (Channel<T> channel: channels)
          { channel.set(null);
          }
        }
        else
        {
          if (val.size()>channels.length)
          { 
            throw new AccessException
              ("Supplied values List is larger ("+val.size()+")"
              +" than the bound expression list ("+channels.length+")"
              );
          }
          
          int i=0;
          for (Channel<T> channel: channels)
          { 
            if (i<val.size())
            { channel.set(val.get(i));
            }
            else
            { channel.set(null);
            }
          }
          
        }
        return true;
      }
      
      @Override
      public boolean isWritable()
      { 
        for (Channel<T> channel: channels)
        { 
          if (!channel.isWritable())
          { return false;
          }
        }
        return true;
      }
      
    };
    

  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append(prefix).append("List: ");
    prefix=prefix+"  ";

    if (sources!=null)
    {
      out.append(prefix).append("(");
      for (int i=0;i<sources.size();i++)
      { 
        if (i>0)
        { out.append(prefix).append(",");
        }
        sources.get(i).dumpTree(out,prefix);
      }
      out.append(prefix).append(")");
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+"{"+sources.toString()+"}";
  }

}
