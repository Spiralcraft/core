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
package spiralcraft.sax;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.text.ParsePosition;
import spiralcraft.util.EmptyIterator;
import spiralcraft.util.tree.Tree;

/**
 * Represents a portion of an XML document
 */
public abstract class Node
  implements Tree<Node,Object>
{
  private LinkedList<Node> _children;
  private Node _parent;
  private Object _peer;
  private StandardPrefixResolver prefixResolver;
  private ParsePosition position;


  /**
   * Associate an application specific Object with this
   *   node.
   */
  @Override
  public void set(Object peer)
  { _peer=peer;
  }
  
  /**
   * Return the application specific Object
   *   associated with this node.
   *   
   */
  @Override
  public Object get()
  { return _peer;
  }
  
  @Override
  public boolean isLeaf()
  { return _children==null || _children.isEmpty();
  }
  
  @Override
  public void removeChild(Node child)
  { _children.remove(child);
  }

  public void remove()
  { 
    if (_parent!=null)
    { _parent.removeChild(this);
    }
  }

  /**
   * Returns a direct reference to the internal list of child Nodes
   * 
   * @return
   */
  public List<Node> getChildList()
  { return _children;
  }
  
  public int getChildCount()
  { 
    if (_children!=null)
    { return _children.size();
    }
    else
    { return 0;
    }
  }
  
  @Override
  public Iterator<Node> iterator()
  { 
    if (_children!=null)
    { return _children.iterator();
    }
    else
    { return new EmptyIterator<Node>();
    }
  }
  
  /**
   * Returns a new copy of the list of child nodes
   */
  @Override
  public Node[] getChildren()
  { 
    if (_children!=null)
    { return _children.toArray(new Node[_children.size()]);
    }
    else
    { return null;
    }
  }

  @SuppressWarnings("unchecked") // Runtime type check
  public <X extends Node> List<X> getChildren(Class<X> type)
  { 
    if (_children==null)
    { return null;
    }

    List<X> ret=new ArrayList<X>();
    for (Node child:_children)
    {
      if (type.isAssignableFrom(child.getClass()))
      { ret.add((X) child);
      }
    }
    return ret;
  }

  /**
   * Remove children including and after the specified child node. 
   * 
   * @param afterChild
   */
  public void truncate(int index)
  { 
    if (_children!=null)
    { 
      while (_children.size()>index)
      { _children.removeLast();
      }
    }
  }
  
  @Override
  public void addChild(Node child)
  { 
    if (_children==null)
    { _children=new LinkedList<Node>();
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

  @Override
  public void setParent(Node parent)
  { _parent=parent;
  }

  @Override
  public Node getParent()
  { return _parent;
  }

  public Node getChild(int index)
  { 
    if (_children!=null)
    { return _children.get(index);
    }
    else
    { throw new IndexOutOfBoundsException(index+">0");
    }
  }
  
  /**
   * 
   * @return The namespace PrefixResolver in effect for this Element
   */
  public StandardPrefixResolver getPrefixResolver()
  { 
    if (this.prefixResolver!=null)
    { return this.prefixResolver;
    }
    else if (_parent!=null)
    { return _parent.getPrefixResolver();
    }
    return null;
  }
  
  /**
   * The namespace PrefixResolver applicable to this Element
   * @param resolver
   */
  public void setPrefixResolver(StandardPrefixResolver resolver)
  { this.prefixResolver=resolver;
  }
  
  public void setPosition(ParsePosition position)
  { this.position=position;
  }
  
  public ParsePosition getPosition()
  { return position;
  }
  
  protected void playChildEvents(ContentHandler handler)
    throws SAXException
  {
    if (_children!=null)
    {
      for (Node child:_children)
      { child.playEvents(handler);
      }
    }
  }

  public abstract void playEvents(ContentHandler handler)
    throws SAXException;
}
