package spiralcraft.vfs.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import spiralcraft.vfs.UnresolvableURIException;

public class HttpURLResource
  extends URLResource
{

  public HttpURLResource(URI uri)
    throws UnresolvableURIException
  { super(uri);
  }
  
  @Override
  public HttpURLMessage getMessage()
    throws IOException
  { 
    final HttpURLConnection connection=(HttpURLConnection) _url.openConnection();
    setupConnection(connection);
    connection.setDoInput(true);
    connection.connect();
    
    InputStream inputStream
      =connection.getInputStream();
    try
    {
      return new HttpURLMessage
        (inputStream
        ,connection.getContentLength()
        ,connection.getHeaderFields()
        ,connection.getResponseCode()
        ,connection.getResponseMessage()
        );
    }
    finally
    { 
      
      inputStream.close();
      if (connection instanceof HttpURLConnection)
      { ((HttpURLConnection) connection).disconnect();
      }
    }
    
  }  

}
