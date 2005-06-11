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
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

public class MethodCallNode
  extends Node
{

  private final Node _source;
  private final String _identifierName;
  private final Expression[] _parameters;
  private final Node[] _parameterNodes;

  public MethodCallNode(Node source,String identifierName,List parameterList)
  { 
    _source=source;
    _identifierName=identifierName;
    _parameters=new Expression[parameterList.size()];
    _parameterNodes=new Node[parameterList.size()];
    parameterList.toArray(_parameterNodes);
    for (int i=0;i<_parameterNodes.length;i++)
    { _parameters[i]=new Expression(_parameterNodes[i],null);
    }
  }

  public Optic bind(final Focus focus)
    throws BindException
  { 
    Optic source=_source.bind(focus);
    Optic ret=source
      .resolve(focus
              ,_identifierName
              ,_parameters
              );
    if (ret==null)
    { throw new BindException("Could not bind method '"+_identifierName+"' operator in "+_source.toString());
    }
    return ret;
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Method");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
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
