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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;


/**
 * A lightweight parse tree of an XML document which captures and preserves SAX events.
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
  private PrefixResolver newPrefixResolver;
  
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
    if (newPrefixResolver==null)
    { 
      newPrefixResolver
        =new PrefixResolver
          (_currentElement!=null
          ?_currentElement.getPrefixResolver()
          :null
          );
    }
    newPrefixResolver.mapPrefix(prefix,uri); 
  }
  
  @Override
  public void endPrefixMapping(String prefix)
  {
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
    Element element=new Element(uri,localName,qName,attributes);
    _currentElement.addChild(element);
    _currentElement=element;
    if (newPrefixResolver!=null)
    { 
      element.setPrefixResolver(newPrefixResolver);
      newPrefixResolver=null;
    }
  }

  @Override
  public void endElement
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { _currentElement=_currentElement.getParent();
  }
  
  @Override
  public void characters
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { _currentElement.addChild(new Characters(ch,start,length));
  }

  @Override
  public void ignorableWhitespace
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { _currentElement.addChild(new IgnorableWhitespace(ch,start,length));
  }
}
