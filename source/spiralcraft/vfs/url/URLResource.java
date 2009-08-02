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
package spiralcraft.vfs.url;

import spiralcraft.log.ClassLog;
import spiralcraft.vfs.AbstractResource;
import spiralcraft.vfs.UnresolvableURIException;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class URLResource
  extends AbstractResource
{
  private static final ClassLog log
    =ClassLog.getInstance(URLResource.class);
  
  private static final boolean debug=false;
  private int timeoutMS=-1;
    
  private final URL _url;
 

  public URLResource(URL url)
  { 
    super(URI.create(url.toString()));
    _url=url;
  }
  
  public URLResource(URI uri)
    throws UnresolvableURIException
  { 
    super(uri);
    if (!uri.isAbsolute())
    { throw new UnresolvableURIException(uri,"Non-absolute URI");
    }
    
    try
    { _url=uri.toURL();
    }
    catch (MalformedURLException x)
    { throw new UnresolvableURIException(uri,"Malformed URL");
    }
  }

  /**
   * Specify the default timeout in milleseconds for IO operations. A value
   *   of -1 indicates that the platform defaults for URLConnection should be
   *   used.
   * 
   * @param timeoutMS
   */
  public void setTimeout(int timeoutMS)
  { this.timeoutMS=timeoutMS;
  }
  
  @Override
  public InputStream getInputStream()
    throws IOException
  { 
    URLConnection connection=_url.openConnection();
    setupConnection(connection);
    connection.setDoInput(true);
    return connection.getInputStream();
  }

  private void setupConnection(URLConnection connection)
  {
    if (timeoutMS>-1)
    { 
      connection.setConnectTimeout(timeoutMS);
      connection.setReadTimeout(timeoutMS);
    }
    connection.setUseCaches(false);
    connection.setAllowUserInteraction(false);
    connection.addRequestProperty("Connection","close");
  }
  
  @Override
  public boolean supportsRead()
  { return true;
  }

  @Override
  public OutputStream getOutputStream()
    throws IOException
  { 
    URLConnection connection=_url.openConnection();
    setupConnection(connection);
    connection.setDoOutput(true);
    return connection.getOutputStream();
  }

  @Override
  public boolean supportsWrite()
  { return true;
  }
  
  @Override
  public boolean exists()
    throws IOException
  {
    URLConnection connection=_url.openConnection();
    setupConnection(connection);
    connection.setDoInput(true);
    connection.setDoOutput(false);
    connection.connect();
    if (debug)
    { log.fine(connection.getContentType());
    }
    return connection.getDate()>0;    
  }
  
  @Override
  public long getSize()
    throws IOException
  {
    URLConnection connection=_url.openConnection();
    setupConnection(connection);
    connection.setDoInput(true);
    connection.setDoOutput(false);
    connection.connect();
    return connection.getContentLength();
    
  }
  
  public void renameTo(URI uri)
    throws IOException
  { throw new UnsupportedOperationException("URL rename not supported "+uri);
  }
  
  public void delete()
    throws IOException
  { throw new IOException("URL Delete is not implemented");
  }
}
