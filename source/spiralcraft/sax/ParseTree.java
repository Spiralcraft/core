package spiralcraft.sax;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.io.IOException;

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
  private Node _currentNode;
  
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

  public void startDocument()
    throws SAXException
  { 
    _document=new Document();
    _currentNode=_document;
  }

  public void endDocument()
    throws SAXException
  { 
    _document.complete();
    _currentNode=null;
  }
   
  public void startElement
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  {
    Element element=new Element(uri,localName,qName,attributes);
    _currentNode.addChild(element);
    _currentNode=element;
  }

  public void endElement
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { _currentNode=_currentNode.getParent();
  }
  
  public void characters
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { _currentNode.addChild(new Characters(ch,start,length));
  }

  public void ignorableWhitespace
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { _currentNode.addChild(new IgnorableWhitespace(ch,start,length));
  }
}
