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

import java.util.List;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

public class MethodCallNode
  extends Node
{

  private final Node _source;
  private final String _identifierName;
  private final Expression[] _parameters;
  private final Node[] _parameterNodes;

  
  @SuppressWarnings("unchecked") // Raw type for parameter node array
  public MethodCallNode(Node source,String identifierName,List<Node> parameterList)
  { 
    
    _source=source;
    if (_source==null)
    { throw new IllegalArgumentException("MethodCallNode: Source cannot be null");
    }
    
    _identifierName=identifierName;
    _parameters=new Expression[parameterList.size()];
    _parameterNodes=new Node[parameterList.size()];
    parameterList.toArray(_parameterNodes);
    for (int i=0;i<_parameterNodes.length;i++)
    { _parameters[i]=new Expression(_parameterNodes[i],null);
    }

//    System.out.println("MethodCallNode "+toString()+" init: resolving against "+_source);
//    for (Node node:parameterList)
//    { System.out.println("MethodCallNode parameter "+node.toString());
//    }

//    debugTree(System.err);
  }

  /**
   * MethodCallNode operates on a source. If there is no direct source,
   *   the subject of the supplied focus will be used.
   */
  public Channel bind(final Focus focus)
    throws BindException
  { 
    Channel<?> source;
    if (_source!=null)
    { source=_source.bind(focus);
    }
    else
    { 
      // This should not happen
      throw new BindException("InternalException: MethodCallNode- no source");
    }

//    System.out.println("MethodCallNode "+toString()+" bind: resolving against "+source);
//    for (Expression param: _parameters)
//    { System.out.println("MethodCallNode "+toString()+" param:"+param.toString());
//    }
    
    Channel ret=source
      .resolve(focus
              ,_identifierName
              ,_parameters
              );

//    System.out.println("MethodCallNode "+toString()+" bound to "+ret);
    
    if (ret==null)
    { throw new BindException("Could not bind method '"+_identifierName+"' operator in "+_source.toString());
    }
    return ret;
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append(prefix).append("Method: ").append(_identifierName);
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
    else
    { out.append(prefix).append("(source is null)");
    }
    if (_parameters!=null)
    {
      out.append(prefix).append("(");
      for (int i=0;i<_parameters.length;i++)
      { 
        if (i>0)
        { out.append(prefix).append(",");
        }
        _parameterNodes[i].dumpTree(out,prefix);
      }
      out.append(prefix).append(")");
    }
  }
  
  public String toString()
  { return super.toString()+":"+_identifierName+"(...)";
  }

}
