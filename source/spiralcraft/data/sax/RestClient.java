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
package spiralcraft.data.sax;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.SAXException;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayTuple;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Assignment;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLogger;

import spiralcraft.text.html.URLDataEncoder;

/**
 * <p>Interactes with a web service that uses a REST-like interface
 * </p>
 * 
 * @author mike
 *
 */
public class RestClient
{
  private static final ClassLogger log
    =ClassLogger.getInstance(RestClient.class);

  private URI baseURI;
  private AttributeBinding<?>[] urlQueryBindings;
  private ThreadLocalChannel<Tuple> queryChannel;
  private RootFrameHandler<?> handler;
  private Assignment<?>[] assignments;
  private Setter<?>[] setters;
  
  private Focus<?> focus;
  private boolean debug;
  
  /**
   * <p>Provide the Handler which translates the query response
   * </p>
   * 
   * @param handler
   */
  public void setRootFrameHandler(RootFrameHandler<?> handler)
  { this.handler=handler;
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
  
  public void setAssignments(Assignment<?>[] assignments)
  { this.assignments=assignments;
  }
  
  
  /**
   * <p>Resolve all expressions
   * </p>
   * 
   * @param parentFocus
   * @throws BindException
   */
  public void bind(Focus<?> parentFocus)
    throws BindException
  {

    
    queryChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(handler.getType())
        );
    
    focus=new SimpleFocus<Tuple>(parentFocus,queryChannel);
    
    handler.setFocus(focus);
    handler.bind();
    bindAttributes();
    bindAssignments();
  }

  protected void bindAssignments()
    throws BindException
  { 
    if (assignments!=null)
    { setters=Assignment.bindArray(assignments, focus);
    }
    
  }
  
  protected void bindAttributes()
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
   * <p>Run the query from the supplied query object.
   * </p>
   * 
   * <p>The query object is updated with current values according to
   *   the Assignments, and the items specified in the AttributeBindings
   *   are incorporated into the REST query URL.
   * </p>
   */
  public Tuple query(Tuple query)
    throws DataException
  {
    if (query==null)
    { query=new EditableArrayTuple(handler.getType());
    }
    
    // Push the query object
    queryChannel.push(query);
    try
    {
      
      if (setters!=null)
      { Setter.applyArray(setters);
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
      return (Tuple) dataReader.readFromURI(queryURI,handler.getType());
    }
    catch (SAXException x)
    { throw new DataException("Error reading response",x);
    }
    catch (IOException x)
    { throw new DataException("I/O error performing query",x);
    }
    finally
    { queryChannel.pop();
    }
  }
  
}
