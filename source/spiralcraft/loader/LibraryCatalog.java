package spiralcraft.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;

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
  private ArrayList discoverLibraries()
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

    ArrayList list=new ArrayList(20);

    for (int i=0;i<jars.length;i++)
    {
      Library lib=new Library();
      lib.path=jars[i].getAbsolutePath();
      lib.lastModified=jars[i].lastModified();
      list.add(lib);
      System.err.println(lib.path+" : "+lib.lastModified);

    }
    return list;
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
}

