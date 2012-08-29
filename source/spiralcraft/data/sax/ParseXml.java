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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.xml.sax.SAXException;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.types.standard.AnyType;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.sax.XmlWriter;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;
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
  extends Scenario<Void,Tresult>
{
  

  private RootFrame<Tresult> handler;
  protected Binding<URI> uriX;
  protected Channel<Resource> resourceChannel;
  protected boolean preBuffer;
  protected boolean ignoreEmpty;
  protected boolean useContextualResource;
  
  public ParseXml()
  {
  }
  
  public ParseXml(Type<Tresult> type,Expression<URI> uriX)
    throws BindException
  { 
    setType(type);
    Binding<URI> uriBinding=new Binding<URI>(uriX);
    uriBinding.setTargetType(URI.class);
    setUriX(uriBinding);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ParseXml(Reflector<Tresult> reflector,Expression<URI> uriX)
    throws ContextualException
  { 
    this
      ((reflector instanceof DataReflector)
        ?((DataReflector) reflector).getType()
        :ReflectionType.canonicalType(reflector.getContentType()
        )
      ,uriX
      );
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ParseXml(Reflector<Tresult> reflector)
    throws ContextualException
  { 
    this
      ((reflector instanceof DataReflector)
        ?((DataReflector) reflector).getType()
        :ReflectionType.canonicalType(reflector.getContentType()
        )
      );
  }

  public ParseXml(Type<Tresult> type) 
    throws ContextualException
  {
    setType(type);
    useContextualResource=true;
  }
  
  protected Type<Tresult> type;
  { storeResults=true;
  }
    
  @Override
  public Task task()
  { return new ParseTask();
  }
  
  public void setUriX(Binding<URI> uriX)
  { this.uriX=uriX;
  }
  
  public void setType(Type<Tresult> type)
    throws BindException
  { 
    this.type=type;
    this.resultReflector=DataReflector.getInstance(type);
  }
  
  /**
   * <p>Buffer the entire document.
   * </p>
   * 
   * @param preBuffer
   */
  public void setPreBuffer(boolean preBuffer)
  { this.preBuffer=preBuffer;
  }
  
  /**
   * <p>Ignores an empty input document
   * </p>
   * 
   * <p>When set to true, will also pre-buffer the document
   * </p>
   * 
   * @param ignoreEmpty
   */
  public void setIgnoreEmpty(boolean ignoreEmpty)
  { this.ignoreEmpty=ignoreEmpty;
  }
  
  
  @Override
  protected Focus<?> bindImports(Focus<?> focus)
    throws BindException
  {
    if (ignoreEmpty)
    { preBuffer=true;
    }
    
    if (resultReflector==null)
    { 
      try
      {
        resultReflector
          =DataReflector.getInstance(Type.resolve(AnyType.TYPE_URI));
      }
      catch (DataException x)
      { throw new BindException("Error binding type "+AnyType.TYPE_URI);
      }
    }
    return focus;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> focus)
    throws ContextualException
  {
    focus=super.bindExports(focus);
    
    if (uriX!=null)
    { uriX.bind(focus);
    }

    if (handler!=null)
    {
      handler.setFocus(focus);
      handler.bind();
      if (handler.getType()!=null)
      { 
        this.type=handler.getType();
        resultReflector=DataReflector.getInstance(handler.getType());
      }
    }
    if (useContextualResource)
    { resourceChannel=LangUtil.assertChannel(Resource.class,focus);
    }
    return focus;
  }
  
  
 
  
  /**
   * The FrameHandler that handles the root or document level XML content 
   * 
   * @param handler
   */
  public void setRootFrameHandler(RootFrame<Tresult> handler)
  { this.handler=handler;
  }
  
  protected Tresult read(URI uri)
    throws IOException,UnresolvableURIException,DataException,SAXException
  { 
    Resource resource;
    if (uri==null && resourceChannel!=null)
    { 
      resource=resourceChannel.get();
      uri=resource.getURI();
    }
    else
    { resource=Resolver.getInstance().resolve(uri);
    }
    return read(uri,resource);
  }
  
  protected Tresult read(URI uri,Resource resource)
    throws IOException,SAXException,DataException
  {
    if (debug)
    { log.fine("Opening "+uri);
    }
    InputStream in=resource.getInputStream();
    if (preBuffer)
    { 
      ByteArrayOutputStream buffer=new ByteArrayOutputStream();
      StreamUtil.copyRaw(in,buffer,64*1024);
      buffer.flush();
      in.close();
      
      byte[] bytes=buffer.toByteArray();
      if (bytes.length==0 && ignoreEmpty)
      { 
        if (debug)
        { log.fine("Closing (empty) "+uri);
        }
        return null;
      }
      in=new ByteArrayInputStream(buffer.toByteArray());
    }
    try
    { 
      return parse(in,newDataReader(),uri);
    }
    finally
    { 
      in.close();
      if (debug)
      { log.fine("Closing "+uri);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  protected Tresult parse(InputStream in,DataReader reader,URI uri)
    throws IOException,DataException,SAXException
  {
    if (handler!=null)
    { return (Tresult) reader.readFromInputStream(in,handler.getType(),uri);
    }
    else
    { return (Tresult) reader.readFromInputStream(in,type,uri);
    }
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
    
  protected URI getDefaultURI()
  { return null;
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
    extends AbstractTask
  {

    public ParseTask()
    { 
    }
        
    @Override
    public void work()
      throws InterruptedException
    { 
      try
      { 
        URI uri=null;
        if (!useContextualResource)
        {
          if (uriX!=null)
          { uri=uriX.get();
          }
          if (uri==null)
          { uri=getDefaultURI();
          }
        }
        Tresult result=read(uri);
        if (type!=null)
        { addResult(result);
        }
      }
      catch (Exception x)
      { 
        addException(x);
        return;
      }
    }

  }
}