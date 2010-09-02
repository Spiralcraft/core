//
// Copyright (c) 2010 Michael Toth
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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Computes a parse tree from the stream
 * 
 * @author mike
 *
 */
public class TreeFilter
  extends XmlFilter
{

  protected ParseTree tree;
  
  @Override
  public void setDocumentLocator(Locator locator)
  { 
    tree.setDocumentLocator(locator);
    super.setDocumentLocator(locator);
  }

  public ParseTree getTree()
  { return tree;
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
    tree.startElement(namespaceURI,localName,qName,attribs);
    super.startElement(namespaceURI,localName,qName,attribs);
  }

  @Override
  public void startPrefixMapping(String prefix,String uri)
    throws SAXException
  { 
    tree.startPrefixMapping(prefix, uri);
    super.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix)
    throws SAXException
  { 
    tree.endPrefixMapping(prefix);
    super.endPrefixMapping(prefix);
  }

  @Override
  public void endElement(String namespaceURI,String localName,String qName)
    throws SAXException
  { 
    tree.endElement(namespaceURI,localName,qName);
    super.endElement(namespaceURI,localName,qName);
  }

  
  @Override
  public void characters(char[] ch,int start,int length)
    throws SAXException
  { 
    tree.characters(ch,start,length);
    super.characters(ch,start,length);
  }


  @Override
  public void ignorableWhitespace(char[] ch,int start,int length)
    throws SAXException
  { 
    tree.ignorableWhitespace(ch, start, length);
    super.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target,String data)
    throws SAXException
  { 
    tree.processingInstruction(target,data);
    super.processingInstruction(target,data);
  }

  @Override
  public void skippedEntity(String name)
    throws SAXException
  { 
    tree.skippedEntity(name);
    super.skippedEntity(name);
  }


  @Override
  public void startDocument()
    throws SAXException
  { 
    tree.startDocument();
    super.startDocument();
  }

  @Override
  public void endDocument()
    throws SAXException
  { 
    tree.endDocument();
    super.endDocument();
  }
  
 
  
}
