package spiralcraft.sax;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Represents a portion of an XML document
 */
public abstract class Node
{
  private LinkedList _children;
  private Node _parent;

  public void removeChild(Node child)
  { _children.remove(child);
  }

  public void remove()
  { 
    if (_parent!=null)
    { _parent.removeChild(this);
    }
  }

  public List getChildren()
  { return _children;
  }

  public List getChildren(Class type)
  { 
    if (_children==null)
    { return null;
    }

    List ret=new ArrayList();
    Iterator it = _children.iterator();
    while (it.hasNext())
    {
      Node child=(Node) it.next();
      if (type.isAssignableFrom(child.getClass()))
      { ret.add(child);
      }
    }
    return ret;
  }

  public void addChild(Node child)
  { 
    if (_children==null)
    { _children=new LinkedList();
    }
    _children.add(child);
    child.setParent(this);
  }

  public boolean hasChildren()
  { return _children!=null;
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

  protected void playChildEvents(ContentHandler handler)
    throws SAXException
  {
    if (_children!=null)
    {
      Iterator it=_children.iterator();
      while (it.hasNext())
      { ((Node) it.next()).playEvents(handler);
      }
    }
  }

  public abstract void playEvents(ContentHandler handler)
    throws SAXException;
}
