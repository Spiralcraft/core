package spiralcraft.lang;

import spiralcraft.lang.parser.Node;

public class Expression
{
  private Node _root;

  public Expression(Node root)
  { _root=root;
  }


  public void dumpParseTree(StringBuffer out)
  { _root.dumpTree(out,"\r\n");
  }
}
