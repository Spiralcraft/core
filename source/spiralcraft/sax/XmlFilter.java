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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;


/**
 * Writes XML as SAX events are fired through the ContentHandler interface
 * 
 * When connected to a SAX parser, this class should write an exact copy
 *   of the document being read.
 */
public class XmlFilter
  implements ContentHandler
{
  protected ContentHandler out;
  protected XmlFilter next;
  private Locator _locator;  

  public XmlFilter()
  { 
  }
  
  public void setContentHandler(ContentHandler out)
  { this.out=out;
  }
  
  public void setNext(XmlFilter next)
  { 
    this.out=next;
    this.next=next;
  }
  
  public XmlFilter getLast()
  { 
    if (next!=null)
    { return next.getLast();
    }
    else
    { return this;
    }
  }
  
  @Override
  public void setDocumentLocator(Locator locator)
  { 
    this._locator=locator;
    out.setDocumentLocator(locator);
  }

  public Locator getDocumentLocator()
  { return _locator;
  }
  

  
  @Override
  public void startElement
    (String namespaceURI
    ,String localName
    ,String qName
    ,Attributes attribs
    )
    throws SAXException
  { 
    
    out.startElement(namespaceURI,localName,qName,attribs);
  }

  @Override
  public void startPrefixMapping(String prefix,String uri)
    throws SAXException
  { out.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix)
    throws SAXException
  { out.endPrefixMapping(prefix);
  }

  @Override
  public void endElement(String namespaceURI,String localName,String qName)
    throws SAXException
  { out.endElement(namespaceURI,localName,qName);
  }

  
  @Override
  public void characters(char[] ch,int start,int length)
    throws SAXException
  { out.characters(ch,start,length);
  }


  @Override
  public void ignorableWhitespace(char[] ch,int start,int length)
    throws SAXException
  { out.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target,String data)
    throws SAXException
  { out.processingInstruction(target,data);
  }

  @Override
  public void skippedEntity(String name)
    throws SAXException
  { out.skippedEntity(name);
  }


  @Override
  public void startDocument()
    throws SAXException
  { out.startDocument();
  }

  @Override
  public void endDocument()
    throws SAXException
  { out.endDocument();
  }

}
