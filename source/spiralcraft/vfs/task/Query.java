//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.vfs.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.DictionaryBinding;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.text.html.URLDataEncoder;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.url.URLResource;

/**
 * Retrieves the content of a Resource after dynamically generating the
 *   query string portion of the URI.
 * 
 * @author mike
 *
 */
public class Query<Tresult>
  extends Scenario<Void,Tresult>
{

  public enum OperationType
  {
    GET
    ,PUT
    ,DELETE
    ;
  }
  
  public interface Operation<Tresult>
  {
    abstract Tresult perform(URI uri,Resource resource)
      throws IOException;
  }
  
  public class GetOperation
    implements Operation<Tresult>
  { 
    @Override
    public Tresult perform(URI uri,Resource resource)
      throws IOException
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
        return readStream(in,uri);
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
    protected Tresult readStream(InputStream in,URI uri)
      throws IOException
    { return (Tresult) StreamUtil.readBytes(in);
    }
    
  }
  
  public class DeleteOperation
    implements Operation<Tresult>
  {
    @Override
    public Tresult perform(URI uri,Resource resource)
      throws IOException
    {
      if (debug)
      { log.fine("Deleting "+uri);
      }
      resource.delete();
      return null;
    }     
  }
  
  
  private static final int DEFAULT_TIMEOUT_SECONDS=10;
  protected DictionaryBinding<?>[] uriQueryBindings;
  private int timeoutSeconds;  
  private int inputBufferLength;
  private boolean preBuffer;
  private boolean ignoreEmpty;
  protected Binding<URI> uriX;
  private Binding<String> pathX;
  private Operation<Tresult> operation;
  private OperationType operationType=OperationType.GET;

  { storeResults=true;
  }
    
  
  public void setPathX(Binding<String> pathX)
  { this.pathX=pathX;
  }
  
  @Override
  public Task task()
  { return new QueryTask();
  }

  public void setUriX(Binding<URI> uriX)
  { this.uriX=uriX;
  }
  
  /**
   * <p>Buffer the entire return stream
   * </p>
   * 
   * @param preBuffer
   */
  public void setPreBuffer(boolean preBuffer)
  { this.preBuffer=preBuffer;
  }  

  /**
   * Number of seconds to wait for a response before throwing an
   *   exception. Defaults to 10 seconds. For no timeout, specify a value
   *   of -1.
   * 
   * @param timeoutMs
   */
  public void setTimeoutSeconds(int timeoutSeconds)
  { this.timeoutSeconds=timeoutSeconds;
  }
  
  /**
   * The size of the input buffer, when using URLResources
   */
  public void setInputBufferLength(int inputBufferLength)
  { this.inputBufferLength=inputBufferLength;
  }
  
  /**
   * <p>Specify the set of query string variables (attributes)
   *   and their data bindings for the query URL.
   * </p>
   *   
   * @param queryBindings
   */
  public void setUriQueryBindings(DictionaryBinding<?>[] uriQueryBindings)
  { this.uriQueryBindings=uriQueryBindings;
  }
  
  protected URI getDefaultURI()
  { return null;
  }
  
  
  public void setOperation(Operation<Tresult> operation)
  { this.operation=operation;
  }
  
  public void setOperationType(OperationType type)
  { this.operationType=type;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    if (debug)
    { log.fine("Binding "+getClass());
    }
    if (timeoutSeconds==0)
    { timeoutSeconds=DEFAULT_TIMEOUT_SECONDS;
    }
    focusChain=super.bind(focusChain);
    
    if (operation==null)
    { operation=resolveOperation(operationType);
    }
    if (operation==null)
    { throw new ContextualException("No operation specified");
    }
    return focusChain;
  }
  
  
  protected Operation<Tresult> resolveOperation(OperationType type)
  {
    switch (type)
    {
      case GET:
        return new GetOperation();
      case DELETE:
        return new DeleteOperation();
    }
    return null;

  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> focus)
    throws BindException
  {
    if (ignoreEmpty)
    { preBuffer=true;
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

    if (pathX!=null)
    { pathX.bind(focus);
    }
    
    // Query bindings need access to the command context
    if (uriQueryBindings!=null)
    {      
      for (DictionaryBinding<?> binding: uriQueryBindings)
      { binding.bind(focus);
      }
      
    }
    return focus;
  }
  
  /**
   * Override to generate the query string portion of the request URI. The
   *   default behavior iterates through the UriQueryBindings, encoding
   *   each name + "=" +encodedValue.
   * 
   * @param baseQueryString
   * @return
   */
  protected String completeQueryString(String baseQueryString)
  {
    StringBuilder queryString=new StringBuilder();
    if (baseQueryString!=null)
    { queryString.append(baseQueryString);
    }
    if (uriQueryBindings!=null)
    {
      for (DictionaryBinding<?> binding:uriQueryBindings)
      {
        String[] values=binding.arrayGet();
        
        if (values!=null)
        {
          for (String value : values)
          {
            if (queryString.length()>0)
            { queryString.append("&");
            }
            queryString.append(binding.getName()).append("=");
            String encodedValue=URLDataEncoder.encode(value);
            queryString.append(encodedValue);
            if (debug)
            { log.fine("Encoded ["+value+"] to ["+encodedValue+"]");
            }
            
          }
        }
      }
    }
    return queryString.toString();
    
  }
  
  protected Tresult read(URI baseURI)
    throws IOException,UnresolvableURIException
  {
    URI queryURI=baseURI;
    if (baseURI==null)
    { throw new IllegalArgumentException("Base URI is null- nowhere to query");
    }
    String original=baseURI.getRawQuery();
    String query=completeQueryString(original);
    
    if (query.length()>0)
    { queryURI=URIUtil.replaceRawQuery(baseURI,query);
    }
    
    Resource resource=Resolver.getInstance().resolve(queryURI);
    if (resource instanceof URLResource)
    { 
      
      URLResource urlResource=((URLResource) resource);
      if (timeoutSeconds>0)
      { urlResource.setTimeout(timeoutSeconds*1000);
      }
      if (inputBufferLength>0)
      { urlResource.setInputBufferLength(inputBufferLength);
      }
      
    }

    return read(queryURI,resource);    
  }
  
  protected Tresult read(URI uri,Resource resource)
    throws IOException
  { return operation.perform(uri,resource);
  }  
  


  public class QueryTask
    extends AbstractTask
  {

    public QueryTask()
    { 
    }
      
    @Override
    public void work()
      throws InterruptedException
    { 
      try
      { 
        URI uri=null;
        if (uriX!=null)
        { uri=uriX.get();
        }
        if (uri==null)
        { uri=getDefaultURI();
        }
        if (pathX!=null)
        { 
          String path=pathX.get();
          if (path!=null)
          { 
            uri=URIUtil.ensureTrailingSlash(uri);
            uri=uri.resolve(path);
          }
        }
        Tresult result=read(uri);
        addResult(result);
      }
      catch (Exception x)
      { 
        addException(x);
        return;
      }
    }

  }
  
}
