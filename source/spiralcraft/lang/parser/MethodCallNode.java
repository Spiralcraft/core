package spiralcraft.lang.parser;

import java.util.List;

public class MethodCallNode
  extends Node
{

  private final ResolveNode _source;
  private final Node[] _parameters;

  public MethodCallNode(ResolveNode source,List parameterList)
  { 
    _source=source;
    _parameters=new Node[parameterList.size()];
    parameterList.toArray(_parameters);
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
      _parameters[i].dumpTree(out,prefix);
    }
    out.append(prefix).append(")");
  }

}
