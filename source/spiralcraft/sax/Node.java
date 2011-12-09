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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.text.ParsePosition;
import spiralcraft.util.tree.AbstractNode;

/**
 * Represents a portion of an XML document
 */
public abstract class Node
  extends AbstractNode<Node,Object>
{
  private StandardPrefixResolver prefixResolver;
  private ParsePosition position;
 
  /**
   * Returns a new copy of the list of child nodes
   */
  @Override
  public Node[] getChildren()
  { 
    List<Node> children=getChildList();
    if (children!=null)
    { return children.toArray(new Node[children.size()]);
    }
    else
    { return null;
    }
  }


  @Override
  public Node getParent()
  { return super.getParent();
  }

  @Override
  public Node getChild(int index)
  { return super.getChild(index);
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
    else if (getParent()!=null)
    { return getParent().getPrefixResolver();
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
    for (Node child:this)
    { child.playEvents(handler);
    }
  }

  public abstract void playEvents(ContentHandler handler)
    throws SAXException;
}
