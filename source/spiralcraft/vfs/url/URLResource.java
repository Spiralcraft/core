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

import spiralcraft.io.DebugInputStream;
import spiralcraft.io.InputStreamWrapper;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.vfs.AbstractResource;
import spiralcraft.vfs.UnresolvableURIException;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class URLResource
  extends AbstractResource
{
  private static final ClassLog log
    =ClassLog.getInstance(URLResource.class);
  
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(URLResource.class,null);
  
  private int timeoutMS=-1;
  
    
  private final URL _url;
  private int inputBufferLength;
 

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

  public void setInputBufferLength(int inputBufferLength)
  { this.inputBufferLength=inputBufferLength;
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
    final URLConnection connection=_url.openConnection();
    setupConnection(connection);
    connection.setDoInput(true);
    connection.connect();
    if (debugLevel.canLog(Level.DEBUG))
    { dumpHeaders("On getInputStream()",connection.getHeaderFields());
    }
    if (connection instanceof HttpURLConnection)
    {
      final HttpURLConnection httpConnection=(HttpURLConnection) connection;
      InputStream source=httpConnection.getInputStream();

      if (debugLevel.canLog(Level.FINE))
      { source=new DebugInputStream(source);
      }
      
      if (inputBufferLength>0)
      { 
        if (debugLevel.canLog(Level.DEBUG))
        { log.debug("inputBufferLength="+inputBufferLength);
        }
        source=new BufferedInputStream(source,inputBufferLength);
      }
   
          
      return new InputStreamWrapper(source)
      {        
        @Override
        public void close()
          throws IOException
        {
          
          super.close();
          httpConnection.disconnect();
          if (debugLevel.canLog(Level.DEBUG))
          { log.fine("Disconnected "+_url);
          }
        }
      };
      
    }
    else
    { return connection.getInputStream();      
    }
  }

  private void dumpHeaders(String message,Map<String,List<String>> headers)
  {
    StringBuffer headerBuf=new StringBuffer();
    for (Map.Entry<String,List<String>> entry : headers.entrySet())
    { 
      headerBuf.append("\r\n"+entry.getKey()+"="+entry.getValue());
    }
    log.fine(message+": Response headers for "+_url+":"+headerBuf.toString());
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
    connection.setRequestProperty("Connection","close");
//    if (connection instanceof HttpURLConnection)
//    {
//      HttpURLConnection httpConnection=(HttpURLConnection) connection;
//      
//    }
    
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
    connection.connect();
    if (debugLevel.canLog(Level.DEBUG))
    { dumpHeaders("On getOutputStream()",connection.getHeaderFields());
    }
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
    if (debugLevel.canLog(Level.DEBUG))
    { dumpHeaders("On exists()",connection.getHeaderFields());
    }
    return connection.getDate()>0;    
  }
  
  @Override
  public long getSize()
    throws IOException
  {
    URLConnection connection=_url.openConnection();
    setupConnection(connection);
    connection.setDoInput(false);
    connection.setDoOutput(false);
    connection.connect();
    if (debugLevel.canLog(Level.DEBUG))
    { dumpHeaders("On getSize()",connection.getHeaderFields());
    }
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
