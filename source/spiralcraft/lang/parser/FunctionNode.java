package spiralcraft.lang.parser;

public class FunctionNode
  extends Node
{
  private final Node context;
  private final Node body;
  private String typeQName;
  
  public FunctionNode(String typeQName,Node context,Node body)
  { 
    this.context=context;
    this.body=body;
  }

  @Override
  public Node copy(
    Object visitor)
  { 
    Node context=this.context.copy(visitor);
    Node body=this.body.copy(visitor);
    if (context==this.context && body==this.body)
    { return this;
    }
    else
    { return new FunctionNode(typeQName,context,body);
    }
  }

  @Override
  public Node[] getSources()
  { return new Node[] {context,body};
  }

  @Override
  public String reconstruct()
  { return "[#"+typeQName+context.reconstruct()+"]"+body.reconstruct();
  }

  @Override
  public void dumpTree(
    StringBuffer out,
    String prefix)
  {
    out.append(prefix+"[#"+typeQName);
    context.dumpTree(out,prefix+"  ");
    out.append(prefix+"]");
    body.dumpTree(out,prefix+"  ");
    
  }

}
