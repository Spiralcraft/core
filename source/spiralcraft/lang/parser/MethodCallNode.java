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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

public class MethodCallNode
  extends Node
{

  private final Node _source;
  private final String _identifierName;
  private final Expression<?>[] _parameters;
  private final Node[] _parameterNodes;
  

  
  @SuppressWarnings("rawtypes")
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
    { _parameters[i]=Expression.create(_parameterNodes[i]);
    }

//    System.out.println("MethodCallNode "+toString()+" init: resolving against "+_source);
//    for (Node node:parameterList)
//    { System.out.println("MethodCallNode parameter "+node.toString());
//    }

//    debugTree(System.err);
  }

  @Override
  public Node[] getSources()
  { 
    Node[] ret=new Node[_parameterNodes.length+1];
    ret[0]=_source;
    System.arraycopy(_parameterNodes,0,ret,1,_parameterNodes.length);
    return ret;
  }
  
  @Override
  public Node copy(Object visitor)
  {
    boolean dirty=false;
    List<Node> params=new ArrayList<Node>();
    for (Node node:_parameterNodes)
    { 
      Node paramCopy=node.copy(visitor);
      params.add(paramCopy);
      if (node!=paramCopy)
      { dirty=true;
      }
    }
    MethodCallNode copy
      =new MethodCallNode(_source.copy(visitor),_identifierName,params);
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
    StringBuilder builder=new StringBuilder();
    builder.append(_source.reconstruct());
    builder.append("."+(_identifierName!=null?_identifierName:""));
    builder.append(" ( ");
    boolean first=true;
    for (Node node:_parameterNodes)
    { 
      if (first)
      { first=false;
      }
      else
      { builder.append(" , ");
      }
      builder.append(node.reconstruct());
    }
    builder.append(" ) ");
    return builder.toString();
  }  
  /**
   * MethodCallNode operates on a source. If there is no direct source,
   *   the subject of the supplied focus will be used.
   */
  @Override
  public Channel<?> bind(final Focus<?> focus)
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
    
    if (_identifierName=="" && !source.getReflector().isFunctor())
    { throw new BindException("Not a functor: "+source.getReflector());
    }
    
    Channel<?> ret=null;
    try
    {
      ret=source
        .resolve(focus
                ,_identifierName
                ,_parameters
                );
    }
    catch (RuntimeException x)
    { 
      throw new BindException
        ("Could not bind method '"
        +(_identifierName!=null
          ?_identifierName
          :"(functor)"
          )
        +"' in "
        +_source.toString()
        ,x
        );
    }

//    System.out.println("MethodCallNode "+toString()+" bound to "+ret);
    
    if (ret==null)
    { 
      throw new BindException
        ("Could not bind method '"
        +(_identifierName!=null
          ?_identifierName
          :"(functor)"
          )
        +"' in "
        +_source.toString()
        );
    }
    
    
    return ret;
  }
  
  @Override
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
  
  @Override
  public String toString()
  { return super.toString()+":"+_identifierName+"(...)";
  }

}
