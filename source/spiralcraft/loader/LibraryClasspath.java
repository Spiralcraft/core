package spiralcraft.loader;

import java.io.IOException;
import java.net.URL;

/**
 * Resolves class and resource data from a set of libraries
 *   in a LibraryCatalog
 */
public interface LibraryClasspath
{

  public byte[] loadData(String path)
    throws IOException;
  
  public URL getResource(String path)
    throws IOException;

  public void addLibrary(String path)
    throws IOException;

  /**
   * Resolve the most recent library which contains the resource, and
   *   all the other libraries on which that library depends.
   */
  public void resolveLibrariesForResource(String name)
    throws IOException;

  /**
   * Add the libraries associated with the specified module
   *   to the class path
   */
  public void addModule(String moduleName)
    throws IOException;
    
  /**
   * Release any resources we have allocated, such as open libraries
   */
  public void release();
}
