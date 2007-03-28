//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.sax;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import spiralcraft.data.Type;
import spiralcraft.data.DataException;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


/**
 * Reads SAX events into a Data graph.
 */
public class DataReader
{
  
  public Object readFromURI(URI uri,Type formalType)
    throws SAXException,IOException,DataException
  { 
    return readFromResource
      (Resolver.getInstance().resolve(uri)
      ,formalType
      );
  }
  
  public Object readFromResource
    (Resource resource
    ,Type formalType
    )
    throws SAXException,IOException,DataException
  {
    if (!resource.exists())
    { throw new IOException("Resource not found: "+resource.getURI());
    }
    InputStream in=resource.getInputStream();
    try
    { return readFromInputStream(in,formalType,resource.getURI());
    }
    finally
    { 
      if (in!=null)
      { in.close();
      }
    }
  }  
  
  public Object readFromInputStream
    (InputStream in
    ,Type formalType
    ,URI resourceURI
    )
    throws SAXException,IOException,DataException
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
    
    DataHandler handler=new DataHandler(formalType,resourceURI);
    try
    { parser.parse(in,handler);
    }
    catch (SAXException x)
    {
      if (x.getException()!=null)
      {
        if (x.getException() instanceof DataException)
        { throw (DataException) x.getException();
        }
        else
        { throw x;
        }
      }
      else
      { throw x;
      }
    }
    return handler.getCurrentObject();

  }  
  
}