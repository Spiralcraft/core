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
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.BeanReflector;

public class ListNode<T>
  extends Node
{

  private final List<Node> sources;
  
  public ListNode(List<Node> sources)
  { this.sources=sources;
  }

  /**
   * MethodCallNode operates on a source. If there is no direct source,
   *   the subject of the supplied focus will be used.
   */
  @SuppressWarnings("unchecked")
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
    final Channel<T>[] channels=new Channel[sources.size()];
    
    int i=0;
    for (Node node:sources)
    { channels[i++]=(Channel<T>) node.bind(focus);
    }
    
    return new AbstractChannel<List<T>>
      (BeanReflector.<List<T>>getInstance(List.class))
    {

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
  
  public String toString()
  { return super.toString()+"{"+sources.toString()+"}";
  }

}
