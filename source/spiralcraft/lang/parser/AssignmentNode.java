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


import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.AssignmentChannel;


/**
 * <p>Assigns the value of the source Node to the target node whenever the
 *   bound Channel's get() method is called and returns the value that was
 *   assigned. If assignment fails, the value retrieved from the source
 *   channel is still returned.
 * </p>
 * 
 * <p>When the Channel's set() method is called and both the source and
 *   target Channels are writable, the source channel's set() method will
 *   be called with the new value. If the method returns successful, the 
 *   target channels set() method will be called. 
 * </p>
 * 
 * @author mike
 *
 * @param <Ttarget>
 * @param <Tsource>
 */
public class AssignmentNode<Ttarget,Tsource extends Ttarget>
  extends Node
{

  private final Node source;
  private final Node target;
  private final Character op;

  public AssignmentNode(Node target,Node source)
  { 
    this.source=source;
    this.target=target;
    this.op=null;
  }
  
  public AssignmentNode(Node target,Node source,char op)
  { 
    this.source=source;
    this.target=target;
    this.op=op;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {source,target};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    AssignmentNode<Ttarget,Tsource> copy
      =new AssignmentNode<Ttarget,Tsource>
        (target.copy(visitor),source.copy(visitor),op);
    if (copy.target==target && copy.source==source)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return target.reconstruct()+" "+(op!=null?op:"")+"= "+source.reconstruct();
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
  { 
    if (op==null)
    {
      return new AssignmentChannel(source.bind(focus),target.bind(focus));
    }
    else
    { 
      return new AssignmentChannel
        (new BinaryOpNode(target,source,op).bind(focus)
        ,target.bind(focus)
        );
    }
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Assignment =");
    prefix=prefix+"  ";
    target.dumpTree(out,prefix);
    out.append(prefix);
    if (op!=null)
    { out.append(op);
    }
    out.append("=");
    source.dumpTree(out,prefix);
  }


}
