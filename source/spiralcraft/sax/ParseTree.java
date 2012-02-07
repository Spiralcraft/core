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

import java.net.URI;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.text.ParsePosition;
import spiralcraft.util.string.StringPool;


/**
 * A lightweight parse tree of an XML document which captures and preserves SAX
 *  events.
 *
 * Intended as a convenient non-event-driven alternative for applications that
 *   wish to avoid the complexity of DOM- ie. applications that are oriented
 *   more towards manipulating the information contained in XML documents
 *   as opposed to the specific textual representation of that information.
 */
public class ParseTree
  extends DefaultHandler
{
  
  private Document _document;
  private Node _currentElement;
  private StandardPrefixResolver newPrefixResolver;
  private final ParsePosition position=new ParsePosition();
  private Locator locator;
  private final StringPool stringPool=StringPool.INSTANCE;
  
  public static ParseTree createTree(Element root)
  { return new ParseTree(new Document(root));
  }

  public ParseTree()
  {
  }

  public ParseTree(Document document)
  { _document=document;
  }

  public void playEvents(ContentHandler handler)
    throws SAXException
  { _document.playEvents(handler);
  }

  public Document getDocument()
  { return _document;
  }

  @Override
  public void startDocument()
    throws SAXException
  { 
    _document=new Document();
    _currentElement=_document;
  }

  @Override
  public void endDocument()
    throws SAXException
  { 
    _document.complete();
    _currentElement=null;
  }
   
  @Override
  public void startPrefixMapping(String prefix,String uri)
  { 
    prefix=stringPool.get(prefix);
    uri=stringPool.get(uri);
    
    if (newPrefixResolver==null)
    { 
      newPrefixResolver
        =new StandardPrefixResolver
          (_currentElement!=null
          ?_currentElement.getPrefixResolver()
          :null
          );
    }
    newPrefixResolver.mapPrefix(prefix,URI.create(uri)); 
  }
  
  @Override
  public void endPrefixMapping(String prefix)
  {
  }
 
  public void setDocumentURI(URI documentURI)
  { position.setContextURI(documentURI);
  }
  
  @Override
  public void setDocumentLocator(Locator locator)
  { 
    this.locator=locator;
    position.setLine(locator.getLineNumber());
    position.setColumn(locator.getColumnNumber());
  }
  
  private void updatePosition()
  {
    position.setLine(locator.getLineNumber());
    position.setColumn(locator.getColumnNumber());
  }
  
  @Override
  public void startElement
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  {
    uri=stringPool.get(uri);
    localName=stringPool.get(localName);
    qName=stringPool.get(qName);
    
    updatePosition();
    Element element=new Element(uri,localName,qName,attributes);
    _currentElement.addChild(element);
    _currentElement=element;
    if (newPrefixResolver!=null)
    { 
      element.setPrefixResolver(newPrefixResolver);
      newPrefixResolver=null;
    }
    element.setPosition(position.clone());
  }

  @Override
  public void endElement
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    updatePosition();
    _currentElement=_currentElement.getParent();
  }
  
  @Override
  public void characters
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { 
    updatePosition();
    Characters node=new Characters(ch,start,length);
    node.setPosition(position.clone());
    _currentElement.addChild(node);
  }

  @Override
  public void ignorableWhitespace
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { 
    updatePosition();
    IgnorableWhitespace node=new IgnorableWhitespace(ch,start,length);
    node.setPosition(position.clone());
    _currentElement.addChild(node);
  }
}
