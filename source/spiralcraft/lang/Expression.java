package spiralcraft.lang;

import spiralcraft.lang.parser.Node;

public class Expression
{
  private Node _root;

  public Expression(Node root)
  { _root=root;
  }

  public Channel createChannel(Focus focus)
    throws BindException
  { return new Channel(_root.bind(focus)); 
  }

  public void dumpParseTree(StringBuffer out)
  { _root.dumpTree(out,"\r\n");
  }
}
