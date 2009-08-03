//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.xml.sax.SAXException;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.sax.AttributeBinding;
import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.RootFrame;

import spiralcraft.data.spi.EditableArrayTuple;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Assignment;

import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.ThreadedFocusChainObject;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;

import spiralcraft.sax.XmlWriter;

import spiralcraft.text.html.URLDataEncoder;

import spiralcraft.util.thread.ContextFrame;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;
import spiralcraft.vfs.url.URLResource;

/**
 * <p>Interacts with a web service that uses a REST-like interface
 * </p>
 * 
 * <p>This component is thread-safe
 * </p>
 * 
 * @author mike
 *
 */
public class RestClient
  implements ThreadedFocusChainObject
{
  private static final ClassLog log
    =ClassLog.getInstance(RestClient.class);

  private URI baseURI;
  private AttributeBinding<?>[] urlQueryBindings;
  private ThreadLocalChannel<Tuple> localQueryChannel;
  private RootFrame<?> handler;
  private Assignment<?>[] preAssignments;
  private Assignment<?>[] postAssignments;
  private Setter<?>[] preSetters;
  private Setter<?>[] postSetters;
  private Expression<Tuple> queryDataObject;
  private Channel<Tuple> queryDataChannel;
  private int timeoutSeconds;
  
//  private Focus<Tuple> focus;
  private boolean debug;

  // Related to ThreadContext 
  private ContextFrame nextFrame;
  
  /**
   * <p>Provide the Handler which translates the query response
   * </p>
   * 
   * @param handler
   */
  public void setRootFrameHandler(RootFrame<?> handler)
  { this.handler=handler;
  }
  
  /**
   * Number of milliseconds to 
   * @param timeoutMs
   */
  public void setTimeoutSeconds(int timeoutSeconds)
  { this.timeoutSeconds=timeoutSeconds;
  }
  
  /**
   * @return The spiralcraft.data.Type of the data object which provides the
   *   query parameters and optionally caches the result
   */
  public Type<?> getQueryDataType()
  { return handler.getType();
  }
  
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>Specify the base URI to use for all requests from this client.
   * </p>
   * 
   * @param baseURI
   */
  public void setBaseURI(URI baseURI)
  { this.baseURI=baseURI;
  }
  
  /**
   * <p>Specify the set of query string variables (attributes)
   *   and their data bindings for the REST query URL.
   * </p>
   *   
   * @param queryBindings
   */
  public void setURLQueryBindings(AttributeBinding<?>[] urlQueryBindings)
  { this.urlQueryBindings=urlQueryBindings;
  }
  
  /**
   * <p>Specify the set of assignments that will be applied before 
   *   the query is executed
   * </p>
   * 
   * @param assignments
   */
  public void setPreAssignments(Assignment<?>[] preAssignments)
  { this.preAssignments=preAssignments;
  }
  
  /**
   * <p>Specify the set of assignments that will be applied after the query 
   *   completes
   * </p>
   * 
   * @param assignments
   */
  public void setPostAssignments(Assignment<?>[] postAssignments)
  { this.postAssignments=postAssignments;
  }
  
  public void setQueryDataObject(Expression<Tuple> queryDataObject)
  { this.queryDataObject=queryDataObject;
  }
  
 
  
  /**
   * <p>Resolve all expressions
   * </p>
   * 
   * @param parentFocus
   * @throws BindException
   */
  @SuppressWarnings("unchecked") // Checking wildcard Focus for right object
  public Focus<?> bind(Focus<?> parentFocus)
    throws BindException
  {

    if (parentFocus!=null)
    {
      if (queryDataObject!=null)
      { queryDataChannel=parentFocus.bind(queryDataObject);
      }
      else
      { 
        if (parentFocus.isFocus(handler.getType().getURI()))
        { queryDataChannel=(Channel<Tuple>) parentFocus.getSubject();
        }
      }
    }
    
    localQueryChannel
        =new ThreadLocalChannel<Tuple>
          (DataReflector.<Tuple>getInstance(handler.getType())
        );
    
    Focus<?> focus=new SimpleFocus<Tuple>(parentFocus,localQueryChannel);
    
    handler.setFocus(focus);
    handler.bind();
    bindAttributes(focus);
    bindAssignments(focus);
    return focus;
  }

  
  protected void bindAssignments(Focus<?> focus)
    throws BindException
  { 
    if (preAssignments!=null)
    { preSetters=Assignment.bindArray(preAssignments, focus);
    }
    
    if (postAssignments!=null)
    { postSetters=Assignment.bindArray(postAssignments, focus);
    }
  }
  
  protected void bindAttributes(Focus<?> focus)
    throws BindException
  {
    if (urlQueryBindings!=null)
    {      
      for (AttributeBinding<?> binding: urlQueryBindings)
      { binding.bind(focus);
      }
      
    }
  }  
  
  
  /**
   * <p>Run the query, using the supplied query channel.
   * </p>
   * 
   * <p>The query object is updated with current values according to
   *   the Assignments, and the items specified in the AttributeBindings
   *   are incorporated into the REST query URL.
   * </p>
   * 
   * <p>This method is thread-safe
   * </p>
   */
  public void query()
    throws DataException
  {
    if (queryDataChannel!=null)
    { queryDataChannel.set(query(queryDataChannel.get()));
    }
    else
    { 
      throw new DataException
        ("No queryDataObject specified for type "+handler.getType().getURI());
    }
  }
  
  /**
   * <p>Run the query from the supplied query object.
   * </p>
   * 
   * <p>The query object is updated with current values according to
   *   the Assignments, and the items specified in the AttributeBindings
   *   are incorporated into the REST query URL.
   * </p>
   * 
   * <p>This method is thread-safe
   * </p>
   */
  public Tuple query(final Tuple query)
    throws DataException
  { 
    try
    { 
      return runInContext
        (new Delegate<Tuple>()
        {
          @Override
          public Tuple run()
            throws DelegateException
          { 
            try
            { return queryImpl(query);
            }
            catch (DataException x)
            { throw new DelegateException(x);
            }
          }
        }
        );
    }
    catch (DelegateException x)
    { 
      if (x.getCause() instanceof DataException)
      { throw (DataException) x.getCause();
      }
      else
      { throw new RuntimeException(x);
      }
    }
  }  

  private Tuple queryImpl(Tuple query)
    throws DataException
  {
    try
    {
   
      if (query==null && queryDataChannel!=null)
      { query=queryDataChannel.get();    
      }
      
      if (query==null)
      { query=new EditableArrayTuple(handler.getType());
      }
      localQueryChannel.set(query);
      
      if (preSetters!=null)
      { Setter.applyArray(preSetters);
      }
    
      if (baseURI==null)
      { 
        throw new DataException
          ("RestClient.baseURI has not specified");
      }
      URI queryURI=baseURI;
      StringBuilder queryString=new StringBuilder();
      String original=baseURI.getQuery();
      if (original!=null)
      { queryString.append(original);
      }
      if (urlQueryBindings!=null)
      {
        for (AttributeBinding<?> binding:urlQueryBindings)
        {
          String value=binding.get();
          if (value!=null)
          {
            if (queryString.length()>0)
            { queryString.append("&");
            }
            queryString.append(binding.getAttribute()).append("=");
            queryString.append(URLDataEncoder.encode(value));
          }
        }
      
        if (queryString.length()>0)
        {
          try
          {
            queryURI
              =new URI
              (baseURI.getScheme()
              ,baseURI.getUserInfo()
              ,baseURI.getHost()
              ,baseURI.getPort()
              ,baseURI.getPath()
              ,queryString.toString()
              ,baseURI.getFragment()
              );
          }
          catch (URISyntaxException x)
          { throw new DataException("Invalid query "+queryString.toString(),x);
          }
        }
      }

      if (debug)
      { log.fine("Querying: "+queryURI);
      }
      DataReader dataReader=new DataReader();
      dataReader.setFrameHandler(handler);
      
      ByteArrayOutputStream out=null;
      if (debug)
      { 
        out=new ByteArrayOutputStream();
        dataReader.setTraceHandler(new XmlWriter(out,Charset.defaultCharset()));
      }
      
      URLResource resource=new URLResource(queryURI);
      if (timeoutSeconds>0)
      { resource.setTimeout(timeoutSeconds*1000);
      }
      
      // TODO: 2009-02-11 mike: Add timeout here
      query = (Tuple) dataReader.readFromResource(resource,handler.getType());
      
      if (debug)
      { log.fine(new String(out.toString()));
      }
      
      if (postSetters!=null)
      { Setter.applyArray(postSetters);
      }
      return query;
    }
    catch (SAXException x)
    { 
      if (debug)
      { log.log(ClassLog.FINE,"Error reading response",x);
      }
      throw new DataException("Error reading response",x);
    }
    catch (IOException x)
    { 
      if (debug)
      { log.log(ClassLog.FINE,"I/O error performing query",x);
      }
      throw new DataException("I/O error performing query",x);
    }
    
  }

  @Override
  public <T> T runInContext(
    Delegate<T> delegate)
    throws DelegateException
  {

    
    // Push the query object
    localQueryChannel.push(null);
    try
    { 
      if (nextFrame!=null)
      { return nextFrame.runInContext(delegate);
      }
      else
      { return delegate.run();
      }
    }
    finally
    { localQueryChannel.pop();
    }
  }

  @Override
  public void setNext(
    ContextFrame next)
  { this.nextFrame=next;
    
  }
  
}
