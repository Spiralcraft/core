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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Initiates SAX event streams from URIs, Resources and InputStreams
 */
public class ParseFactory
{
  
  /**
   * Parse from a URI. 
   */
  public void parseURI(URI uri,DefaultHandler handler)
    throws SAXException,IOException
  { parseResource(Resolver.getInstance().resolve(uri),handler);
  }

  /**
   * Parse from a resource. 
   */
  public void parseResource(Resource resource,DefaultHandler handler)
    throws SAXException,IOException
  { 
    InputStream in=resource.getInputStream();
    if (in==null)
    { throw new IOException("Resource does not exist: "+resource.getURI());
    }
    try
    { parseInputStream(in,handler);
    }
    finally
    { 
      if (in!=null)
      { in.close();
      }
    }
  }

  /**
   * Parse from an InputStream.
   */
  public void parseInputStream(InputStream in,DefaultHandler handler)
    throws SAXException,IOException
  {
    if (in==null)
    { return;
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
    parser.parse(in,handler);
  }

}
