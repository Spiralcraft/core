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

import spiralcraft.text.xml.AttributeEncoder;
import spiralcraft.text.xml.XmlEncoder;
import spiralcraft.util.string.StringUtil;

import java.io.Flushable;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.nio.charset.Charset;


/**
 * <p>Writes XML as SAX events are fired through the ContentHandler interface
 * </p>
 * 
 * <p>When connected to a SAX parser, this class should write a copy
 *   of the document being read.
 * </p>
 *   
 * <p>Data is encoded using XML 1.0 semantics
 * </p>
 */
public class XmlWriter
  implements ContentHandler
{
  private Appendable _out;
  private Flushable _flushable;
  
  private Locator _locator;
  private boolean _elementDelimiterPending=false;
  private boolean format;
  private int indentLevel;
  private String indentString="  ";
  private Charset encoding=Charset.forName("UTF-8");
  private final AttributeEncoder attributeEncoder=new AttributeEncoder();
  private final XmlEncoder xmlEncoder=new XmlEncoder();
  private boolean escapeWhitespace=true;
  private boolean fragmentMode=false;
  

  public XmlWriter(OutputStream out,Charset charset)
  { 
    
    if (charset!=null)
    { this.encoding=charset;
    }
    _out=new BufferedWriter(new OutputStreamWriter(out,this.encoding));
    _flushable=(Flushable) _out;
  }
  
  public XmlWriter(Appendable writer,Charset encoding)
  { 
    this._out=writer;
    if (writer instanceof Flushable)
    { _flushable=(Flushable) writer;
    }
    this.encoding=encoding;
  }

  @Override
  public void setDocumentLocator(Locator locator)
  { _locator=locator;
  }

  public Locator getDocumentLocator()
  { return _locator;
  }
  
  /**
   * <p>Don't write the XML header or the root element itself
   * </p>
   * 
   * @param fragmentMode
   */
  public void setFragmentMode(boolean fragmentMode)
  { this.fragmentMode=fragmentMode;
  }
  
  /**
   * <p>Escape whitespace to ensure that data is preserved. Defaults to true.
   * </p>
   * 
   * @param escapeWhitespace
   */
  public void setEscapeWhitespace(boolean escapeWhitespace)
  { this.escapeWhitespace=escapeWhitespace;
  }
  
  /**
   * <p>Format the output by inserting line breaks and indent spaces where
   *   appropriate
   * </p>
   * 
   * @param format
   */
  public void setFormat(boolean format)
  { this.format=format;
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
    checkElementClose();      
    try
    {
      if (fragmentMode && indentLevel==0)
      { 
        indentLevel++;
        return;
      }
      
      if (format)
      { _out.append("\r\n\r\n"+StringUtil.repeat(indentString,indentLevel));
      }
      if (format || fragmentMode)
      { indentLevel++;
      }
      
      _out.append("<");
      _out.append(qName);
      for (int i=0;i<attribs.getLength();i++)
      { 
        _out.append(" ");
        if (format && i>0)
        { _out.append("\r\n"+StringUtil.repeat(indentString,indentLevel));
        }
        _out.append(attribs.getQName(i));
        _out.append("=\"");
        writeAttributeValue(attribs.getValue(i));
        _out.append("\"");
      }
      _elementDelimiterPending=true;
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void startElementContent()
    throws SAXException
  { checkElementClose();
  }
  
  @Override
  public void startPrefixMapping(String prefix,String uri)
  { 
//    System.err.println("XmlWriter.startPrefixMapping("+prefix+","+uri+")");
  }

  @Override
  public void endPrefixMapping(String prefix)
  { 
//    System.err.println("XmlWriter.endPrefixMapping("+prefix+")");
  }

  @Override
  public void endElement(String namespaceURI,String localName,String qName)
    throws SAXException
  { 
    try
    {
      if (format || fragmentMode)
      { indentLevel--;
      }
      if (fragmentMode && indentLevel==0)
      { return;
      }
      if (_elementDelimiterPending)
      { 
        _out.append("/>");
        _elementDelimiterPending=false;
      }
      else
      {
        
        if (format)
        { _out.append("\r\n"+StringUtil.repeat(indentString,indentLevel));
        }
        _out.append("</");
        _out.append(qName);
        _out.append(">");
      }
      
    }
    catch (IOException x)
    { fail(x);
    }
  }

  
  private boolean isWhitespace(char c)
  { return c=='\t' || c=='\r' || c=='\n' || c==' ';
  }
  
  @Override
  public void characters(char[] ch,int start,int length)
    throws SAXException
  { 
    checkElementClose();      
    try
    { 
      if (format)
      {
        while (start<length && isWhitespace(ch[start]))
        { start++;
        }
      }
      
      if (escapeWhitespace)
      { xmlEncoder.encode(new String(ch,start,length),_out);
      }
      else
      { xmlEncoder.encodeRaw(new String(ch,start,length),_out);
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void writeAttributeValue(String value)
    throws SAXException
  { 
    try
    { attributeEncoder.encode(value,_out);
    }
    catch (IOException x)
    { fail(x);
    }
  }

  @Override
  public void ignorableWhitespace(char[] ch,int start,int length)
    throws SAXException
  { 
    checkElementClose();      
    try
    { 
      if (!format)
      { _out.append(new String(ch,start,length));
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }

  @Override
  public void processingInstruction(String target,String data)
  { System.err.println("XmlWriter.processingInstruction("+target+","+data+")");
  }

  @Override
  public void skippedEntity(String name)
  { System.err.println("XmlWriter.skippedEntity("+name+")");
  }


  @Override
  public void startDocument()
    throws SAXException
  { 
    if (fragmentMode)
    { return;
    }
    try
    { 
      _out.append("<?xml version=\"1.0\" ");
      if (encoding!=null)
      { _out.append("encoding=\""+encoding+"\"");
      }
      _out.append("?>\r\n");
    }
    catch (IOException x)
    { fail(x);
    }
  }

  @Override
  public void endDocument()
    throws SAXException
  {
    checkElementClose(); 
    
    // XXX We should flush at a higher level
    if (_flushable!=null)
    {
      try
      { _flushable.flush();
      }
      catch (IOException x)
      { fail(x);
      }
    }
  }

  private void fail(Throwable x)
    throws SAXException
  { 
    x.printStackTrace();
    throw new SAXException("Error writing: "+x);
  }

  private void checkElementClose()
    throws SAXException
  {
    try
    {
      if (_elementDelimiterPending)
      {
        _out.append(">");
        _elementDelimiterPending=false;
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }
}
