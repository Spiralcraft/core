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

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.text.ParsePosition;

/**
 * Represents a portion of an XML document
 */
public abstract class Node
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
  public void setPeer(Object peer)
  { _peer=peer;
  }
  
  /**
   * Return the application specific Object
   *   associated with this node.
   *   
   */
  public Object getPeer()
  { return _peer;
  }
  
  public void removeChild(Node child)
  { _children.remove(child);
  }

  public void remove()
  { 
    if (_parent!=null)
    { _parent.removeChild(this);
    }
  }

  public List<Node> getChildren()
  { return _children;
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

  public void setParent(Node parent)
  { _parent=parent;
  }

  public Node getParent()
  { return _parent;
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
