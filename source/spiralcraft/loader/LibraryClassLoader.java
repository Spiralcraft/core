//
// Copyright (c) 1998,2008 Michael Toth
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

import spiralcraft.main.AbstractClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.URL;

/**
 * <P>Loads classes contained in the Library
 * </P>
 * 
 */
public class LibraryClassLoader
  extends AbstractClassLoader
{
  private LibraryClasspath _libraryClasspath;

  public LibraryClassLoader(LibraryCatalog libraryCatalog)
  { _libraryClasspath=libraryCatalog.createLibraryClasspath();
  }

  @Override
  protected String findLibrary(String name)
  { return _libraryClasspath.findNativeLibrary(name);
  }
  
  @Override
  protected String getRepositoryName()
  { return _libraryClasspath.toString();
  }

  @Override
  protected byte[] loadData(String path)
    throws IOException
  { return _libraryClasspath.loadData(path);
  }

  @Override
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
  
  @Override
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
  
  @Override
  public void shutdown()
  { _libraryClasspath.release();
  }
}
