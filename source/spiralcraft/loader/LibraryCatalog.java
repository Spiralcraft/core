package spiralcraft.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

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
  {
    System.err.println("Master library path "+_masterLibraryPath);
    loadCatalog();
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
    Library lib=new Library();
    lib.path=file.getAbsolutePath();
    lib.jar=lib.path.endsWith(".jar");
    lib.lastModified=file.lastModified();
    _libraries.add(lib);
    catalogResources(lib);
    
    System.err.println(lib.path+" : "+lib.lastModified);
    
  }

  private void catalogResources(Library library)
    throws IOException
  {
    if (library.jar)
    { catalogJar(library);
    }
    else
    { catalogDirectory(library);
    }
  }

  private void catalogJar(Library library)
    throws IOException
  {
    
    JarFile jarFile=
      new JarFile(library.path);
    try
    {
      Enumeration entries=jarFile.entries();
  
      while (entries.hasMoreElements())
      {
        JarEntry jarEntry
          =(JarEntry) entries.nextElement();
        Resource resource
          =new Resource();
        resource.name=jarEntry.getName();
        resource.library=library;
        library.resources.put(resource.name,resource);
        catalogResource(resource);
      }
    }
    finally
    { jarFile.close();
    }
    
  }

  private void catalogDirectory(Library library)
  {
  }

  private void catalogResource(Resource resource)
  { 
    
  }
}

class CatalogEntry
{
  String name;
  Library library;
  
}

class Library
{
  String path;
  long lastModified;
  boolean jar;
  HashMap resources=new HashMap();
}

class Resource
{
  Library library;
  String name;
}

class ApplicationInfo
{
  
}

