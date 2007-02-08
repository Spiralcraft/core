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
package spiralcraft.tuple.sax;

import spiralcraft.tuple.TupleFactory;
import spiralcraft.tuple.SchemeResolver;
import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Buffer;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.TupleException;

import spiralcraft.tuple.spi.ArrayTupleFactory;

import spiralcraft.util.StringConverter;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;


import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Stack;
import java.util.List;
import java.util.LinkedList;

/**
 * Reads SAX events into a Tuple graph.
 * 
 * Tuples are represented in XML as follows:
 *
 * XXX TO IMPLEMENT
 * The outermost XML element read by this reader always represents a Typed 
 *   data object (ie. a Tuple, a Collection or a Primitive). 
 *
 * If the containing element represents a Tuple, the contained elements will
 *   be Field names. Each field will contain zero, one, or more Typed data
 *   objects.
 *
 * If the containing element represents a Collection, the contained element
 *   will be named "values", which will contain zero, on or more Typed data
 *   objects.
 *
 * If the containing element represents a Primitive, it will contain a textual
 *   representation of the value of the Primitive.
 *
 * XXX TO DEPRECATE
 * The outermost XML element read by this reader always represents a Tuple. The
 *   elements contained within the outermost element represent the field values
 *   for the Tuple. An element that represents a field value may contain either
 *   a data value, or one or more Tuples, represented as other elements.
 * /XXX TO DEPRECATE
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
  private DataContext _context;
  private Stack<DataContext> _stack=new Stack<DataContext>();
  private List<Tuple> _tupleList;
  
  /**
   * Construct a TupleReader which uses the specified TupleFactory to 
   *   create Tuples and the specified SchemeResolver to resolve Schemes
   */
  public TupleReader(SchemeResolver schemeResolver,TupleFactory tupleFactory)
  {
    _factory=(tupleFactory!=null)?tupleFactory:new ArrayTupleFactory();
    _resolver=schemeResolver;
  }

  /**
   * Obtain the list of accumulated Tuples read from the
   *   input document, if no handlers have been supplied.
   */
  public List<Tuple> getTupleList()  
  { return _tupleList;
  }
  
  public void startDocument()
    throws SAXException
  { _context=new DataContext();
  }

  public void endDocument()
    throws SAXException
  { _tupleList=_context.tupleData;
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
    { 
      startTuple(uri,localName,qName,attributes);
      _inTuple=true;
    }
    else
    { 
      startField(uri,localName,qName,attributes);
      _inTuple=false;
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
    { 
      endField(uri,localName,qName);
      _inTuple=true;
    }
    else
    { 
      endTuple(uri,localName,qName);
      _inTuple=false;
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
    if (!uri.endsWith("/"))
    { uri=uri.concat("/");
    }
    
    try
    { 
      namespace=new URI(uri);
      if (!namespace.getPath().endsWith("/"))
      { namespace=namespace.resolve(namespace.getPath().concat("/"));
      }
    }
    catch (URISyntaxException x)
    { throw new SAXException("Namespace URI syntax error",x);
    }
      
    URI schemeUri=namespace.resolve(localName);
    Scheme scheme=null;
    try
    { scheme=_resolver.resolveScheme(schemeUri);
    }
    catch (TupleException x)
    { x.printStackTrace();
    }
    
    if (scheme==null)
    { throw new SAXException("Cannot resolve a Scheme for URI "+schemeUri);
    }
    
    _context.tuple=_factory.createBuffer(scheme);
  }

  private void startField    
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  { 
    // XXX Use uri for explicit aspect interface (ie. Collection aspect,
    // xxx   Context aspect) by specifying the relevant interface in the
    // xxx   uri and tag name.
    
    Field field=_context.tuple.getScheme()
      .getFields().findFirstByName(localName);
    if (field==null)
    { 
      throw new SAXException
        ("Field '"+localName+"' not found in scheme "
        +_context.tuple.getScheme().getURI()
        );
    }
    _context.field=field;
    _stack.push(_context);
    _context=new DataContext();

  }

  private void endTuple    
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    // XXX At this point, we should pass the tuple off
    // XXX   to a handler to avoid storing the entire
    // XXX   data set- handler mechanism TBD
    // XXX
    _context.tupleData.add(_context.tuple);
    _context.tuple=null;
  }

  private void endField    
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    DataContext fieldDataContext=_context;
    _context=_stack.pop();
    if (_context.tuple!=null)
    {
      _context.tuple.set
        (_context.field.getIndex()
        ,resolveFieldData(fieldDataContext)
        );
    }
    _context.field=null;
  }

  private void fieldData
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  {
    _context.buffer.append(ch,start,length);
  }
  
  private Object resolveFieldData(DataContext context)
  { 
    if (context.tupleData.size()>0)
    { return context.tupleData;
    }
    else
    { 
      String textValue=context.buffer.toString();
      StringConverter converter
        =StringConverter.getInstance(_context.field.getType().getJavaClass());
      if (converter!=null)
      { return converter.fromString(textValue);
      }
      else if (_context.field.getType().getJavaClass()
                .isAssignableFrom(String.class)
              )
      { return textValue;
      }
      else
      {
        Object ovalue=null;
        try
        { ovalue=StringConverter.decodeFromXml(textValue);
        }
        catch (Exception x)
        { x.printStackTrace();
        }
        if (ovalue!=null)
        { return ovalue;
        }
        else
        {
          System.err.println
            ("Can't convert data value '"
            +textValue
            +"' to "
            +_context.field.getType().getJavaClass()
            );
          return null;
        }
      }
    }
  }
  
  // Represents a value- the contents of a Field element. 
  class DataContext
  { 
    StringBuilder buffer=new StringBuilder();
    List<Tuple> tupleData = new LinkedList<Tuple>();

    Buffer tuple;
    Field field;
    
    
  }
}

