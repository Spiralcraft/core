package spiralcraft.stream;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;

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
	 * Read the resource data.
   *
   *@return An InputStream, or null if the resource can't be read.
   *@throws IOException if there was an unexpected problem reading.
	 */	
  public InputStream getInputStream()
    throws IOException
  { return null;
  }

  /**
   * Indicate whether the specific data encapsulated by this Resource
   *   can be read. This operation may involved network IO.
   *@return Whether the data can be read
   *@throws IOException if there was an unexpected problem finding this out
   */
  public boolean canRead()
    throws IOException
  { return false;
  }

  /**
   * Indicate whether this type of resource supports read operations.
   *@return true if this type of resource supports reading
   */
  public boolean supportsRead()
  { return false;
  }

}
