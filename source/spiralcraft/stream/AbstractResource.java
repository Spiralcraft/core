package spiralcraft.stream;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractResource
  implements Resource
{
  
  private final URI _uri;

  public AbstractResource(URI uri)
  { _uri=uri;
  }

  public URI getURI()
  { return _uri;
  }

	/**
   * Read not supported, throw an IOException
	 */	
  public InputStream getInputStream()
    throws IOException
  { throw new IOException("Resource cannot be read");
  }

  /**
   * Read not supported by default
   */
  public boolean supportsRead()
  { return false;
  }

	/**
   * Throw an IOException, can't write
	 */	
  public OutputStream getOutputStream()
    throws IOException
  { throw new IOException("Resource cannot be written to");
  }

  /**
   * Doesn't know about parents
   */
  public Resource getParent()
  { return null;
  }

  /**
   * Write not supported by default
   */
  public boolean supportsWrite()
  { return false;
  }

  /**
   * Containership not supported
   */
  public Container asContainer()
  { return null;
  }    

  public String toString()
  { return super.toString()+":"+_uri.toString();
  }
}
