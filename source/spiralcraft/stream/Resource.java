package spiralcraft.stream;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;

/**
 * Something the can be accessed using streams.
 */
public interface Resource
{
  /**
   * Return the absolute URI corresponding to this resource
   */
  public URI getURI();

	/**
	 * Read the resource data.
   *
   *@return An InputStream, or null if the resource can't be read.
   *@throws IOException if there was an unexpected problem reading.
	 */	
	public InputStream getInputStream()
		throws IOException;

  /**
   * Indicate whether the specific data encapsulated by this Resource
   *   can be read. This operation may involved network IO.
   *@return Whether the data can be read
   *@throws IOException if there was an unexpected problem finding this out
   */
  public boolean canRead()
    throws IOException;

  /**
   * Indicate whether this type of resource supports read operations.
   *@return true if this type of resource supports reading
   */
  public boolean supportsRead();
}
