package spiralcraft.stream.url;

import spiralcraft.stream.AbstractResource;
import spiralcraft.stream.UnresolvableURIException;

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
  private URL _url;

  public URLResource(URI uri)
    throws UnresolvableURIException
  { 
    super(uri);
    try
    { _url=uri.toURL();
    }
    catch (MalformedURLException x)
    { throw new UnresolvableURIException(uri,"Malformed URL");
    }
  }

  public InputStream getInputStream()
    throws IOException
  { return _url.openStream();
  }

  public boolean supportsRead()
  { return true;
  }

  public OutputStream getOutputStream()
    throws IOException
  { 
    URLConnection connection=_url.openConnection();
    connection.setDoOutput(true);
    return connection.getOutputStream();
  }

  public boolean supportsWrite()
  { return true;
  }
  
  public boolean exists()
    throws IOException
  {
    URLConnection connection=_url.openConnection();
    connection.setDoInput(false);
    connection.setDoOutput(false);
    connection.connect();
    return connection.getDate()>0;
  }
}
