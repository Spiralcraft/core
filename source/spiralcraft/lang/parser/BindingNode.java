//
// Copyright (c) 2009 Michael Toth
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

import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.BindingChannel;


/**
 * <p>Indicates that the location specified by a target expression should
 *   receive the value of the source channel at some processing point.
 * </p>
 * 
 * <p>The target expression is relative to a different Focus that the
 *   source channel. The source channel will be bound automatically in
 *   the usual context, creating a BindingChannel. The target expression must
 *   be bound in an additional step by supplying the appropriate "local" 
 *   Focus to the BindingChannel.bindTarget() method.
 * </p>
 * 
 * <p>Every read of the BindingChannel assigns the current value of the
 *   source channel to the target channel.
 * </p>
 * 
 * <p>Every write of the BindingChannel assigns a new value to both the
 *   source and the target channel
 * </p>
 
 * 
 * @author mike
 *
 * @param <Ttarget>
 * @param <Tsource>
 */
public class BindingNode<Ttarget,Tsource extends Ttarget>
  extends Node
{

  private final Node source;
  private final Node target;

  public BindingNode(Node target,Node source)
  { 
    this.source=source;
    this.target=target;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {source,target};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    BindingNode<Ttarget,Tsource> copy
      =new BindingNode<Ttarget,Tsource>
        (target.copy(visitor),source.copy(visitor));
    if (copy.target==target && copy.source==source)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return target.reconstruct()+" := "+source.reconstruct();
  }
  
  public Node getSource()
  { return source;
  }
  
  public Node getTarget()
  { return target;
  }

    
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Heterogeneous operation
  public Channel bind(final Focus focus)
    throws BindException
  { return new BindingChannel(source.bind(focus),new Expression(target));
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Binding =");
    prefix=prefix+"  ";
    target.dumpTree(out,prefix);
    out.append(prefix).append(":=");
    source.dumpTree(out,prefix);
  }

}
