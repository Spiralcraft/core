//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.net.URL;

import spiralcraft.util.StringUtil;

import spiralcraft.command.CommandContext;

/**
 * Catalog of the code and resource libraries available for use by 
 * this process. 
 */
public class LibraryCatalog
{


  private final String _masterLibraryPath;

  private ArrayList _libraries=new ArrayList();
  
  /**
   * File libraryDir
   */
  public LibraryCatalog(File path)
  { 
    _masterLibraryPath=path.getAbsolutePath();
    loadCatalog();
  }
  
  public List listLibraries()
  { return _libraries;
  }
  
  public CommandContext newCommandContext()
  { return new LibraryCatalogCommandContext(this);
  }
  
  public void close()
  {
    Iterator it=_libraries.iterator();
    while (it.hasNext())
    { 
      try
      { 
        Library library=(Library) it.next();
        library.forceClose();
      }
      catch (IOException x)
      { }
    }
  }
  
  /**
   * Create a LibraryClasspath to access a subset of the catalog
   */
  public LibraryClasspath createLibraryClasspath()
  { return new LibraryClasspathImpl();
  }

  public Library findLibrary(String fileName)
  {
    Library library
      =getLibrary
        (new File(_masterLibraryPath+File.separator+fileName)
          .getAbsolutePath()
        );
    return library;

  }

  private Library getLibrary(String fullPath)
  { 
    Iterator it=_libraries.iterator();
    while (it.hasNext())
    { 
      Library library=(Library) it.next();
      if (library.path.equals(fullPath))
      { return library;
      }
    }
    return null;
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
    File[] libs
      =new File(_masterLibraryPath)
        .listFiles
          (new FilenameFilter()
          {
            public boolean accept(File dir,String name)
            { 
              return name.endsWith(".jar")
                || name.endsWith(".dll")
                || name.endsWith(".so")
                ;
            }
          }
          );

    _libraries.clear();

    if (libs!=null)
    {
      for (int i=0;i<libs.length;i++)
      { catalogLibrary(libs[i]);
  
      }
    }

  }

  private void catalogLibrary(File file)
    throws IOException
  {
    Library lib;
    if (file.getName().endsWith(".jar"))
    { lib=new JarLibrary(file);
    }
    else if (file.getName().endsWith(".dll")
            || file.getName().endsWith(".so")
            )
    { lib=new NativeLibrary(file);
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
    private final HashMap _resources=new HashMap();
    private final ArrayList _myLibraries=new ArrayList();

    public void release()
    {
      Iterator it=_myLibraries.iterator();
      while (it.hasNext())
      {
        Library library=(Library) it.next();
        try
        { library.close();
        }
        catch (IOException x)
        { }
      }
      _myLibraries.clear();
      _resources.clear();
    }
    
    public byte[] loadData(String path)
      throws IOException
    {
      Resource resource=(Resource) _resources.get(path);
      if (resource==null)
      { throw new IOException("Not found: "+path);
      }
      return resource.getData();
    }

    public URL getResource(String path)
      throws IOException
    {
      Resource resource=(Resource) _resources.get(path);
      if (resource==null)
      { return null;
      }
      return resource.getResource();
    }

    /**
     * Adds the libraries which contain the specified module 
     *   and its dependents to the set of libraries available
     *   to the classloader.
     */
    public void addModule(String name)
      throws IOException
    { 
      LinkedList libraries=new LinkedList();
      Iterator it=_libraries.iterator();
      while (it.hasNext())
      {
        Library library=(Library) it.next();
        if (library.isModule(name))
        { libraries.add(library);
        }
      }
      
      if (libraries.size()==0)
      { throw new IOException("Module not found: "+name);
      }
      
      addLibrary((Library) libraries.get(0));
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
      
      String[] dependencies
        =library.getLibraryDependencies();

      if (dependencies!=null)
      {
        for (int i=0;i<dependencies.length;i++)
        { 
          Library depends=findLibrary(dependencies[i]);
          if (depends!=null)
          { addLibrary(depends);
          }
          else
          { 
            throw new IOException
              ("Unsatisified dependency "+dependencies[i]+" loading "+library.path);
          }
        }
      }
            
    }

    public void resolveLibrariesForResource(String resourcePath)
      throws IOException
    { 
      List libraries=new LinkedList();

      Iterator it=_libraries.iterator();
      while (it.hasNext())
      { 
        Library library=(Library) it.next();

        if (library.resources.get(resourcePath)!=null)
        { libraries.add(library);
        }
      }

      if (libraries.size()==0)
      { throw new IOException("Not found: "+resourcePath);
      }
      
      addLibrary((Library) libraries.get(0));
      
    }
    
    public String findNativeLibrary(String name)
    {
      List libraries=new LinkedList();

      Iterator it=_libraries.iterator();
      while (it.hasNext())
      { 
        Library library=(Library) it.next();

        if ((library instanceof NativeLibrary)
            && library.name.equals(name)
           )
        { return library.path;
        }
      }
      return null;
      
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
  String name;
  long lastModified;
  HashMap resources=new HashMap();

  public Library(File file)
    throws IOException
  { 
    path=file.getAbsolutePath();
    name=file.getName();
    lastModified=file.lastModified();
    catalogResources();
  }

  /**
   * Indicate whether the library is a release of the
   *   specified module
   */
  public boolean isModule(String moduleName)
  { 
    // XXX For now, use exact name = HACK
    return name.equals(moduleName) && !(this instanceof NativeLibrary);
  }

  public abstract String[] getLibraryDependencies();

  public abstract void open()
    throws IOException;

  public abstract void close()
    throws IOException;

  public abstract void forceClose()
    throws IOException;

  public abstract void catalogResources()
    throws IOException;

}

class JarLibrary
  extends Library
{

  int openCount=0;
  JarFile jarFile;
  Manifest manifest;

  public JarLibrary(File file)
    throws IOException
  { 
    super(file);

    name=file.getName();
    if (name.endsWith(".jar"))
    { name=name.substring(0,name.length()-4);
    }
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
    { 
      jarFile=new JarFile(path);
      readManifest();
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

  public synchronized void forceClose()
    throws IOException
  { 
    if (jarFile!=null)
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

  private void readManifest()
    throws IOException
  { manifest=jarFile.getManifest();
  }

  /**
   * Return the list of libraries that this library depends on
   */
  public String[] getLibraryDependencies()
  {
    if (manifest==null)
    { return null;
    }
    
    String classpath
      =manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
    if (classpath!=null)
    { 
      String[] classpathArray
        =StringUtil.tokenize(classpath," ");
      return classpathArray;
    }
    return null;
  }
  

}

class NativeLibrary
  extends Library
{


  public NativeLibrary(File file)
    throws IOException
  { 
    super(file);
    name=file.getName();
    if (name.endsWith(".dll"))
    { name=name.substring(0,name.length()-4);
    }
    else if (name.endsWith(".so"))
    { name=name.substring(0,name.length()-3);
    }
  }

  public void catalogResources()
    throws IOException
  {
  }

  public synchronized void open()
    throws IOException
  { 
  }

  public synchronized void close()
    throws IOException
  {
  }

  public synchronized void forceClose()
    throws IOException
  { 
  }
  
  public String[] getLibraryDependencies()
  { return null;
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

  public void forceClose()
  { 
  }
  
  public String[] getLibraryDependencies()
  { return null;
  }
}

abstract class Resource
{
  Library library;
  String name;

  public abstract byte[] getData()
    throws IOException;

  public abstract URL getResource()
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

  public URL getResource()
    throws IOException
  { return new URL("jar:file:///"+library.path.replace('\\','/')+"!/"+name);
  }
}

class FileResource
  extends Resource
{
  File file;

  public byte[] getData()
  { return null;
  }

  public URL getResource()
    throws IOException
  { return new URL("file:/"+library.path+"/"+name);
  }

}

class ApplicationInfo
{
  
}

