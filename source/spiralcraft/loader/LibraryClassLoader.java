package spiralcraft.loader;

import spiralcraft.util.ArrayUtil;

import spiralcraft.main.AbstractClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.URL;

/**
 * Loads classes contained in the library
 */
public class LibraryClassLoader
  extends AbstractClassLoader
{
  private LibraryClasspath _libraryClasspath;

  public LibraryClassLoader(LibraryCatalog libraryCatalog)
  { _libraryClasspath=libraryCatalog.createLibraryClasspath();
  }

  protected String findLibrary(String name)
  { return _libraryClasspath.findNativeLibrary(name);
  }
  
  protected String getRepositoryName()
  { return _libraryClasspath.toString();
  }

  protected byte[] loadData(String path)
    throws IOException
  { return _libraryClasspath.loadData(path);
  }

  protected URL findResource(String path)
  { 
    try
    { return _libraryClasspath.getResource(path);
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    return null;
  }
  
  public InputStream getResourceAsStream(String path)
  { 
    InputStream in=null;
    ClassLoader parent=getParent();
    if (parent!=null)
    { in=parent.getResourceAsStream(path);
    }
    if (in==null)
    { 
      try
      { in=new ByteArrayInputStream(loadData(path));
      }
      catch (IOException x)
      { }
    }
    return in;
  }

  public void resolveLibrariesForClass(String className)
    throws IOException
  {
    String resourceName=className.replace('.','/')+".class";
    _libraryClasspath.resolveLibrariesForResource(resourceName);
  }

  /**
   * Adds the libraries associated with this module and its
   *   dependents to the classpath
   */
  public void addModule(String moduleName)
    throws IOException
  { _libraryClasspath.addModule(moduleName);
  }
  
  public void shutdown()
  { _libraryClasspath.release();
  }
}
