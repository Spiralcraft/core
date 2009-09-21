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
import java.net.URISyntaxException;

import org.xml.sax.SAXException;

import spiralcraft.data.DataException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.text.html.URLDataEncoder;
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

  private AttributeBinding<?>[] uriQueryBindings;
  private int timeoutSeconds;  
  private int inputBufferLength;

  /**
   * Number of seconds to wait for a response before throwing an
   *   exception
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
    throws BindException
  {
    if (debug)
    { log.fine("Binding "+getClass());
    }
    bindAttributes(focusChain);
    return super.bind(focusChain);
  }
  
  protected void bindAttributes(Focus<?> focus)
    throws BindException
  {
    if (uriQueryBindings!=null)
    {      
      for (AttributeBinding<?> binding: uriQueryBindings)
      { binding.bind(focus);
      }
      
    }
  } 
  
  @Override
  protected void read(URI baseURI)
    throws DataException,SAXException,IOException,UnresolvableURIException
  {
    URI queryURI=baseURI;
    if (baseURI==null)
    { throw new DataException("URI is null- nowhere to query");
    }
    StringBuilder queryString=new StringBuilder();
    String original=baseURI.getQuery();
    if (original!=null)
    { queryString.append(original);
    }
    if (uriQueryBindings!=null)
    {
      for (AttributeBinding<?> binding:uriQueryBindings)
      {
        String[] values=binding.getValues();
        
        if (values!=null)
        {
          for (String value : values)
          {
            if (queryString.length()>0)
            { queryString.append("&");
            }
            queryString.append(binding.getAttribute()).append("=");
            queryString.append(URLDataEncoder.encode(value));
          }
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

    read(queryURI,resource);    
  }
  
  
}
