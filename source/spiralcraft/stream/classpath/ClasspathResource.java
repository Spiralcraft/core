package spiralcraft.stream.classpath;

import spiralcraft.stream.AbstractResource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClasspathResource
  extends AbstractResource
{
  private final ClassLoader _classLoader;
  private final String _path;
  private URL _url;

  public ClasspathResource(URI uri)
    throws UnresolvableURIException
  { 
    super(uri);
    _path=uri.getPath().substring(1);
    _classLoader=Thread.currentThread().getContextClassLoader();
  }

  public InputStream getInputStream()
    throws IOException
  { return _classLoader.getResourceAsStream(_path);
  }

  public boolean supportsRead()
  { return true;
  }

  public OutputStream getOutputStream()
    throws IOException
  { 
    if (_url==null)
    { _url=_classLoader.getResource(_path);
    }
    if (_url==null)
    { throw new IOException("Resource '"+_path+"' cannot be written to");
    }
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
    InputStream in=_classLoader.getResourceAsStream(_path);
    if (in!=null)
    { 
      in.close();
      return true;
    }
    return false; 
  }
}
