package spiralcraft.stream.url;

import spiralcraft.stream.AbstractResource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.IOException;

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

  public boolean canRead()
    throws IOException
  { 
    InputStream in=getInputStream();
    if (in!=null)
    { 
      in.close();
      return true;
    }
    return false;
  }

  public boolean supportsRead()
  { return true;
  }


}
