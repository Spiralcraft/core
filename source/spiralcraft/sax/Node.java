package spiralcraft.sax;

import java.util.List;
import java.util.LinkedList;

/**
 * Represents a portion of an XML document
 */
public class Node
{
  private LinkedList _children;
  private Node _parent;

  public List getChildren()
  { return _children;
  }

  public void addChild(Node child)
  { 
    if (_children==null)
    { _children=new LinkedList();
    }
    _children.add(child);
    child.setParent(this);
  }

  public boolean canHaveChildren()
  { return false;
  }

  public void setParent(Node parent)
  { _parent=parent;
  }

  public Node getParent()
  { return _parent;
  }
}
