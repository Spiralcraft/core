package spiralcraft.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import spiralcraft.util.StringUtil;


/**
 * Catalog of the code and resource libraries available for use by 
 * this process.
 */
public class LibraryCatalog
{


  private String _masterLibraryPath
    =System.getProperty("spiralcraft.home")
    +File.separator
    +"lib";

  private String[] _additionalLibraryPaths
    =StringUtil.tokenize
      (System.getProperty("spiralcraft.class.path")
      ,File.pathSeparator
      );

  private ArrayList _libraries=new ArrayList();

  public LibraryCatalog()
  { loadCatalog();
  }

  /**
   * Create a LibraryClasspath to access a subset of the catalog
   */
  public LibraryClasspath createLibraryClasspath()
  { return new LibraryClasspathImpl();
  }

  /**
   * Load catalog data into memory
   */
  private void loadCatalog()
  { 
    try
    { discoverLibraries();
    }
    catch (IOException x)
    { x.printStackTrace();
    }
  }

  /**
   * Discovers all libraries usable by this catalog
   */
  private void discoverLibraries()
    throws IOException
  { 
    File[] jars
      =new File(_masterLibraryPath)
        .listFiles
          (new FilenameFilter()
          {
            public boolean accept(File dir,String name)
            { return name.endsWith(".jar");
            }
          }
          );
    
    _libraries.clear();

    for (int i=0;i<jars.length;i++)
    { catalogLibrary(jars[i]);

    }

    for (int i=0;i<_additionalLibraryPaths.length;i++)
    { catalogLibrary(new File(_additionalLibraryPaths[i]));
    }

  }

  private void catalogLibrary(File file)
    throws IOException
  {
    Library lib;
    if (file.getName().endsWith(".jar"))
    { lib=new JarLibrary(file);
    }
    else
    { lib=new FileLibrary(file);
    }
    _libraries.add(lib);
  }

  /**
   * Implemetation of LibraryClasspath- uses a subset of the LibraryCatalog
   *   to load classes and resources.
   */
  class LibraryClasspathImpl
    implements LibraryClasspath
  {
    private HashMap _resources=new HashMap();
    private ArrayList _myLibraries=new ArrayList();

    public byte[] loadData(String path)
      throws IOException
    {
      Resource resource=(Resource) _resources.get(path);
      if (resource==null)
      { throw new IOException("Not found: "+path);
      }
      return resource.getData();
    }

    public void addLibrary(String path)
      throws IOException
    { 
      Iterator it=_libraries.iterator();
      boolean found=false;
      while (it.hasNext())
      { 
        Library library=(Library) it.next();

        // Use versioning logic to find the best
        //   library in the future
        if (library.path.equals(path))
        { 
          addLibrary(library);
          found=true;
          break;
        }
      }
      if (!found)
      { throw new IOException("Not found: "+path);
      }
    }

    private void addLibrary(Library library)
      throws IOException
    {
      library.open();
      _myLibraries.add(library);
      _resources.putAll(library.resources);
      // XXX Resolve dependencies
    }

    public void resolveLibrariesForResource(String resourcePath)
      throws IOException
    { 
      Iterator it=_libraries.iterator();
      boolean found=false;
      while (it.hasNext())
      { 
        Library library=(Library) it.next();

        // Use versioning logic to find the best
        //   library in the future
        if (library.resources.get(resourcePath)!=null)
        { 
          addLibrary(library);
          found=true;
          break;
        }
      }
      if (!found)
      { throw new IOException("Not found: "+resourcePath);
      }
      
    }
  }

}

class CatalogEntry
{
  String name;
  Library library;
  
}

abstract class Library
{
  String path;
  long lastModified;
  HashMap resources=new HashMap();

  public Library(File file)
    throws IOException
  { 
    path=file.getAbsolutePath();
    lastModified=file.lastModified();
    catalogResources();
  }

  public abstract void open()
    throws IOException;

  public abstract void close()
    throws IOException;

  public abstract void catalogResources()
    throws IOException;

}

class JarLibrary
  extends Library
{

  int openCount=0;
  JarFile jarFile;

  public JarLibrary(File file)
    throws IOException
  { super(file);
  }

  public void catalogResources()
    throws IOException
  {
    
    jarFile=
      new JarFile(path);
    try
    {
      Enumeration entries=jarFile.entries();
  
      while (entries.hasMoreElements())
      {
        JarEntry jarEntry
          =(JarEntry) entries.nextElement();
        JarResource resource
          =new JarResource();
        resource.entry=jarEntry;
        resource.name=jarEntry.getName();
        resource.library=this;
        resources.put(resource.name,resource);
      }
    }
    finally
    { 
      jarFile.close();
      jarFile=null;
    }
    
  }

  public synchronized void open()
    throws IOException
  { 
    if (openCount==0)
    { jarFile=new JarFile(path);
    }
    openCount++;
  }

  public synchronized void close()
    throws IOException
  {
    openCount--;
    if (openCount==0)
    { jarFile.close();
    }
  }

  public byte[] getData(JarEntry entry)
    throws IOException
  {
    BufferedInputStream in=null;
    try
    {
      in = new BufferedInputStream(jarFile.getInputStream(entry));

      byte[] data = new byte[(int) entry.getSize()];
      in.read(data);
      in.close();
      return data;
    }
    catch (IOException x)
    { 
      if (in!=null)
      {
        try
        { in.close();
        }
        catch (IOException y)
        { }
      }
      throw x;
    }
  }
}

class FileLibrary
  extends Library
{
  public FileLibrary(File file)
    throws IOException
  { super(file);
  }

  public void catalogResources()
  {
  }

  public void open()
  {
  }

  public void close()
  {
  }
}

abstract class Resource
{
  Library library;
  String name;

  public abstract byte[] getData()
    throws IOException;
}

class JarResource
  extends Resource
{
  JarEntry entry;
  
  public byte[] getData()
    throws IOException
  { return ((JarLibrary) library).getData(entry);
  }

}

class FileResource
  extends Resource
{
  File file;

  public byte[] getData()
  { return null;
  }
}

class ApplicationInfo
{
  
}

