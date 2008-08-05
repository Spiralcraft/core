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

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/**
 * Represents an XML Document
 */
public class Document
  extends Node
{

  // private boolean _completed;

  public Document()
  {
  }

  public Document(Element root)
  { addChild(root);
  }

  public void complete()
  { // _completed=true;
  }

  public Element getRootElement()
  { 
    if (getChildren().size()>0)
    { return (Element) getChildren().get(0);
    }
    else
    { return null;
    }
  }

  @Override
  public void playEvents(ContentHandler handler)
    throws SAXException
  { 
    handler.startDocument();
    Node root=getRootElement();
    if (root!=null)
    { root.playEvents(handler);
    }
    handler.endDocument();
  }
  
}
