package spiralcraft.tuple.sax;

import spiralcraft.tuple.TupleFactory;
import spiralcraft.tuple.SchemeResolver;
import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Tuple;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Stack;

/**
 * Reads SAX events into a Tuple graph.
 * 
 * Tuples are represented in XML as follows:
 *
 * The outermost XML element read by this reader always represents a Tuple. The
 *   elements contained within the outermost element represent the field values
 *   for the Tuple. An element that represents a field value may contain either
 *   a data value, or one or more Tuples, represented as other elements.
 *
 * An element which represents a Tuple has a namespace qualified tag name which
 *   resolves to the Scheme of the Tuple in a manner specific to the supplied
 *   SchemeResolver. 
 *
 * An element which represents a field value has a tag name which corresponds
 *   to the field name. 
 */
public class TupleReader
  extends DefaultHandler
{
  private final TupleFactory _factory;
  private final SchemeResolver _resolver;
  private boolean _inTuple=false;
  private Tuple _tuple;
  private Stack _stack;
  
  /**
   * Construct a TupleReader which uses the specified TupleFactory to 
   *   create Tuples and the specified SchemeResolver to resolve Schemes
   */
  public TupleReader(TupleFactory tupleFactory,SchemeResolver schemeResolver)
  {
    _factory=tupleFactory;
    _resolver=schemeResolver;
  }
  
  public void startDocument()
    throws SAXException
  { 
  }

  public void endDocument()
    throws SAXException
  { 
  }
   
  public void startElement
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  { 
    if (!_inTuple)
    { startTuple(uri,localName,qName,attributes);
    }
    else
    { startField(uri,localName,qName,attributes);
    }
  }

  public void endElement
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    if (!_inTuple)
    { endField(uri,localName,qName);
    }
    else
    { endTuple(uri,localName,qName);
    }
  }
  
  public void characters
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  {
    if (!_inTuple)
    { fieldData(ch,start,length);
    }
  }

  public void ignorableWhitespace
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  {
  }  
  
  private void startTuple    
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  {
    URI namespace;
    
    try
    { namespace=new URI(uri);
    }
    catch (URISyntaxException x)
    { throw new SAXException("Namespace URI syntax error",x);
    }
      
    URI schemeUri=namespace.resolve(localName);
    Scheme scheme=_resolver.resolveScheme(schemeUri);
    if (scheme==null)
    { throw new SAXException("Cannot resolve a Scheme for URI "+schemeUri);
    }
    
    _tuple=_factory.createTuple(scheme);
    _stack.push(_tuple);
  }

  private void startField    
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  {
    
  }

  private void endTuple    
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    _tuple=(Tuple) _stack.pop();
  }

  private void endField    
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  {
  }

  public void fieldData
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  {

  }
}
