package spiralcraft.stream.classpath;

import spiralcraft.stream.AbstractResource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.IOException;

public class ClasspathResource
  extends AbstractResource
{
  private ClassLoader _classLoader;
  private String _path;

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
