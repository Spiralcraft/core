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

import java.io.IOException;
import java.net.URI;

import org.xml.sax.SAXException;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.lang.Focus;
import spiralcraft.text.html.URLDataEncoder;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.url.URLResource;

/**
 * A scenario which queries XML by constructing a URI from the context
 *   and translating the result into a Tuple data model.
 * 
 * @author mike
 *
 */
public class QueryXml<Tresult>
  extends ParseXml<Tresult>
{

  private static final int DEFAULT_TIMEOUT_SECONDS=10;
  protected AttributeBinding<?>[] uriQueryBindings;
  private int timeoutSeconds;  
  private int inputBufferLength;

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
   *   and their data bindings for the REST query URL.
   * </p>
   *   
   * @param queryBindings
   */
  public void setUriQueryBindings(AttributeBinding<?>[] uriQueryBindings)
  { this.uriQueryBindings=uriQueryBindings;
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
    
    return focusChain;
  }
  
  
  @Override
  protected Focus<?> bindExports(Focus<?> exportChain)
    throws ContextualException
  { 
    exportChain=super.bindExports(exportChain);
    // Query bindings need access to the command context
    if (uriQueryBindings!=null)
    {      
      for (AttributeBinding<?> binding: uriQueryBindings)
      { binding.bind(exportChain);
      }
      
    }
    return exportChain;
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
    throws DataException
  {
    StringBuilder queryString=new StringBuilder();
    if (baseQueryString!=null)
    { queryString.append(baseQueryString);
    }
    if (uriQueryBindings!=null)
    {
      for (AttributeBinding<?> binding:uriQueryBindings)
      {
        String[] values=binding.arrayGet();
        
        if (values!=null)
        {
          for (String value : values)
          {
            if (queryString.length()>0)
            { queryString.append("&");
            }
            queryString.append(binding.getAttribute()).append("=");
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
  
  @Override
  protected Tresult read(URI baseURI)
    throws DataException,SAXException,IOException,UnresolvableURIException
  {
    URI queryURI=baseURI;
    if (baseURI==null)
    { throw new DataException("URI is null- nowhere to query");
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
  
  
}
