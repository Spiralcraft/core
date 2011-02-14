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

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

import java.net.URI;
import java.nio.charset.Charset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ParseTreeFactory
{

  private static Charset UTF8=Charset.forName("UTF-8");
  
  /**
   * Load a ParseTree from a URI. 
   *
   *@return The ParseTree, or null if the target of the URI does
   *  not exist.
   */
  public static ParseTree fromURI(URI uri)
    throws SAXException,IOException
  { return fromResource(Resolver.getInstance().resolve(uri));
  }

  /**
   * Write a ParseTree to a URI
   */     
  public static void toURI(ParseTree parseTree,URI uri)
    throws SAXException,IOException
  { toResource(parseTree,Resolver.getInstance().resolve(uri));
  }

  /**
   * Load a ParseTree from a resource. 
   *
   *@return The ParseTree
   */
  public static ParseTree fromResource(Resource resource)
    throws SAXException,IOException
  { 
    InputStream in=resource.getInputStream();
    try
    { return fromInputStream(in);
    }
    finally
    { 
      if (in!=null)
      { in.close();
      }
    }
  }

  /**
   * Parse fragmentary XML contained in a String. Encapsulates a String within
   *   &lt;_%gt; tags.
   * 
   * @param string
   * @param filter
   * @return
   * @throws SAXException
   * @throws IOException
   */
  public static ParseTree fromFragment(String string,XmlFilter filter)
    throws SAXException,IOException
  { return fromString("<_>"+string+"</_>",filter);
  }
  
  /**
   * Parse an XML document contained in a UTF-8 string. 
   */
  public static ParseTree fromString(String string,XmlFilter filter)
    throws SAXException,IOException
  { return fromBytes(string.getBytes(UTF8),filter);
  }
  
  /**
   * Parse an XML document contained in a byte[] 
   * 
   * @param bytes
   * @return
   * @throws SAXException
   * @throws IOException
   */
  public static ParseTree fromBytes(byte[] bytes,XmlFilter filter)
    throws SAXException,IOException
  { return fromInputStream(new ByteArrayInputStream(bytes),filter);
  }
  
  /**
   * Write a ParseTree to a resource. 
   */
  public static void toResource(ParseTree parseTree,Resource resource)
    throws SAXException,IOException
  { 
    OutputStream out=resource.getOutputStream();
    if (out==null)
    { throw new IOException("Resource '"+resource+"' cannot be written to");
    }

    try
    { toOutputStream(parseTree,out);
    }
    finally
    { out.close();
    }
  }

  
  /**
   * Write a parse tree to an OutputStream.
   */
  public static void toOutputStream(ParseTree tree,OutputStream out)
    throws SAXException
  {
    XmlWriter writer=new XmlWriter(out,null);
    tree.playEvents(writer);
  }
  
  /**
   * Write a parse tree to an OutputStream.
   */
  public static void toOutputStream
    (ParseTree tree,OutputStream out,XmlFilter filter)
    throws SAXException
  {
    XmlWriter writer=new XmlWriter(out,null);
    filter.getLast().setContentHandler(writer);
    tree.playEvents(filter);
  } 
  
  
  public static void toWriter(ParseTree tree,Appendable out)
    throws SAXException  
  {
    XmlWriter xmlWriter=new XmlWriter(out,null);
    xmlWriter.setEscapeWhitespace(false);
    tree.playEvents(xmlWriter);
  }
  
  public static String toString(ParseTree parseTree)
    throws SAXException
  { 
    StringWriter writer=new StringWriter();
    toWriter(parseTree,writer);
    return writer.toString();
    
  }

  public static String toFragment(ParseTree tree)
    throws SAXException
  { 
    StringWriter writer=new StringWriter();
    XmlWriter xmlWriter=new XmlWriter(writer,null);
    xmlWriter.setEscapeWhitespace(false);
    xmlWriter.setFragmentMode(true);
    tree.playEvents(xmlWriter);    
    return writer.toString();
  
  }

  /**
   * Load a parse tree from an InputStream.
   *@return The ParseTree, or null if the supplied InputStream is null.
   */
  public static ParseTree fromInputStream(InputStream in)
    throws SAXException,IOException
  { return fromInputStream(in,null);
  }
  
  /**
   * Load a parse tree from an InputStream.
   *@return The ParseTree, or null if the supplied InputStream is null.
   */
  public static ParseTree fromInputStream(InputStream in,XmlFilter filter)
    throws SAXException,IOException
  {
    if (in==null)
    { return null;
    }

    
    SAXParserFactory factory=SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);

    SAXParser parser; 
    try
    { parser = factory.newSAXParser(); 
    }
    catch (ParserConfigurationException x)
    { 
      // Shouldn't happen
      x.printStackTrace();
      throw new IOException(x.toString());
    }
    ParseTree parseTree=new ParseTree();
    ContentHandler handler=parseTree;
    if (filter!=null)
    { 
      filter.getLast().setContentHandler(handler);
      handler=filter;
    }
    parser.parse(in,parseTree);
    return parseTree;
  }

 

}
