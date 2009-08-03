//
// Copyright (c) 2008,2009 Michael Toth
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.xml.sax.SAXException;

import spiralcraft.data.DataException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.sax.XmlWriter;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.UnresolvableURIException;



/**
 * <p>Parses XML into a data object
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class ParseXml<Tresult>
  extends Scenario
{
  

  private RootFrame<Tresult> handler;
  private Expression<URI> uriX;
  private Channel<URI> uriChannel;
    
  @Override
  public Task task()
  { return new ParseTask();
  }
  
  public void setUriX(Expression<URI> uriX)
  { this.uriX=uriX;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws BindException
  {
    if (uriX!=null)
    { uriChannel=focus.<URI>bind(uriX);
    }
    handler.setFocus(focus);
    handler.bind();
    return super.bind(focus);
  }
  
  /**
   * The FrameHandler that handles the root or document level XML content 
   * 
   * @param handler
   */
  public void setRootFrameHandler(RootFrame<Tresult> handler)
  { this.handler=handler;
  }
  
  protected void read(URI uri)
    throws IOException,UnresolvableURIException,DataException,SAXException
  { 
    log.fine("Opening "+uri);
    InputStream in=Resolver.getInstance().resolve(uri).getInputStream();
    try
    { 
      parse(in,newDataReader(),uri);
    }
    finally
    { 
      log.fine("Closing "+uri);
      in.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  protected Tresult parse(InputStream in,DataReader reader,URI uri)
    throws IOException,DataException,SAXException
  {
    return (Tresult) reader.readFromInputStream(in,handler.getType(),uri);
  }
  
  protected DataReader newDataReader()
  {
    DataReader dataReader=new DataReader();
    dataReader.setFrameHandler(handler);
    
    ByteArrayOutputStream out=null;
    if (debug)
    { 
      out=new ByteArrayOutputStream();
      dataReader.setTraceHandler(new XmlWriter(out,Charset.defaultCharset()));
    } 
    return dataReader;
  }
    
  /**
   * <p>A Task adapter to parse an XML document
   * </p>
   * 
   * @author mike
   *
   * @param <T>
   * @param <R>
   */
  public class ParseTask
    extends ChainTask
  {

    public ParseTask()
    { 
    }
        
    @Override
    public void work()
      throws InterruptedException
    { 
      try
      { read(uriChannel.get());
      }
      catch (Exception x)
      { 
        addException(x);
        return;
      }
      super.work();
    }

  }
}