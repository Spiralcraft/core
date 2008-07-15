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

import spiralcraft.util.StringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;


/**
 * Writes XML as SAX events are fired through the ContentHandler interface
 * 
 * When connected to a SAX parser, this class should write an exact copy
 *   of the document being read.
 */
public class XmlWriter
  implements ContentHandler
{
  private Writer _writer;
  private Locator _locator;
  private boolean _elementDelimiterPending=false;
  private boolean format;
  private int indentLevel;
  private String indentString="  ";
  

  public XmlWriter(OutputStream out)
  { 
    try
    {
      _writer=new BufferedWriter(new OutputStreamWriter(out,"UTF-8"));
    }
    catch (UnsupportedEncodingException x)
    { 
      // Unfathomable?
      throw new RuntimeException("Platform does not support UTF-8",x);
    }
  }
  
  public void setDocumentLocator(Locator locator)
  { _locator=locator;
  }

  public Locator getDocumentLocator()
  { return _locator;
  }
  
  /**
   * <P>Format the output by inserting line breaks and indent spaces where
   *   appropriate
   * </P>
   * 
   * @param format
   */
  public void setFormat(boolean format)
  { this.format=format;
  }
  
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
      if (format)
      { _writer.write("\r\n\r\n"+StringUtil.repeat(indentString,indentLevel++));
      }
      _writer.write("<");
      _writer.write(qName);
      for (int i=0;i<attribs.getLength();i++)
      { 
        _writer.write(" ");
        if (format && i>0)
        { _writer.write("\r\n"+StringUtil.repeat(indentString,indentLevel));
        }
        _writer.write(attribs.getQName(i));
        _writer.write("=\"");
        writeAttributeValue(attribs.getValue(i));
        _writer.write("\"");
      }
      _elementDelimiterPending=true;
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void startPrefixMapping(String prefix,String uri)
  { System.err.println("XmlWriter.startPrefixMapping("+prefix+","+uri+")");
  }

  public void endPrefixMapping(String prefix)
  { System.err.println("XmlWriter.endPrefixMapping("+prefix+")");
  }

  public void endElement(String namespaceURI,String localName,String qName)
    throws SAXException
  { 
    try
    {
      if (format)
      { indentLevel--;
      }
      if (_elementDelimiterPending)
      { 
        _writer.write("/>");
        _elementDelimiterPending=false;
      }
      else
      {
        if (format)
        { _writer.write("\r\n"+StringUtil.repeat(indentString,indentLevel));
        }
        _writer.write("</");
        _writer.write(qName);
        _writer.write(">");
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }

  private boolean isWhitespace(char c)
  { return c=='\t' || c=='\r' || c=='\n' || c==' ';
  }
  
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
      
      for (int i=start;i<length;i++)
      {

        
        switch (ch[i])
        {
        case '&':
          _writer.write("&amp;");
          break;
        case '<':
          _writer.write("&lt;");
          break;
        case '>':
          _writer.write("&gt;");
          break;
        default:
          _writer.write(ch[i]);
          break;
        }
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void writeAttributeValue(String value)
    throws SAXException
  { 
    char[] chars=value.toCharArray();
    try
    { 
      for (int i=0;i<chars.length;i++)
      {
        switch (chars[i])
        {
        case '&':
          _writer.write("&amp;");
          break;
        case '<':
          _writer.write("&lt;");
          break;
        case '>':
          _writer.write("&gt;");
          break;
        case '"':
          _writer.write("&quot;");
          break;
        case '\r':
          _writer.write("&#xD;");
          break;
        case '\n':
          _writer.write("&#xA;");
          break;
        case '\t':
          _writer.write("&#x9;");
          break;
        default:
          _writer.write(chars[i]);
          break;
        }
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void ignorableWhitespace(char[] ch,int start,int length)
    throws SAXException
  { 
    checkElementClose();      
    try
    { 
      if (!format)
      { _writer.write(ch,start,length);
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void processingInstruction(String target,String data)
  { System.err.println("XmlWriter.processingInstruction("+target+","+data+")");
  }

  public void skippedEntity(String name)
  { System.err.println("XmlWriter.skippedEntity("+name+")");
  }


  public void startDocument()
    throws SAXException
  { 
    try
    {
      _writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
    }
    catch (IOException x)
    { fail(x);
    }
  }

  public void endDocument()
    throws SAXException
  {
    checkElementClose();      
    try
    { _writer.flush();
    }
    catch (IOException x)
    { fail(x);
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
        _writer.write(">");
        _elementDelimiterPending=false;
      }
    }
    catch (IOException x)
    { fail(x);
    }
  }
}
