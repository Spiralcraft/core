package spiralcraft.stream;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
   * Indicate whether this type of resource supports read operations.
   *@return true if this type of resource supports reading
   */
  public boolean supportsRead();

	/**
	 * Write to the resource
   *
   *@return An OutputStream, or null if the resource can't be written to.
   *@throws IOException if there was an unexpected problem opening a stream for
   *        writing.
	 */	
  public OutputStream getOutputStream()
    throws IOException;

  /**
   * Indicate whether this type of resource supports write operations.
   *@return true if this type of resource supports writing
   */
  public boolean supportsWrite();

  /**
   * Return the Container aspect of this Resource,
   *   if that aspect applies to the specific Resource (ie.
   *   the resource is a directory)
   */
  public Container asContainer();

  /**
   * Return the enclosing or containing Resource.
   */
  public Resource getParent();
}
