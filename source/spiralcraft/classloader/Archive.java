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
package spiralcraft.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.file.FileResource;

/**
 * Represents an archive of classes and/or resources, such as a jar file or 
 *   a directory tree.
 * 
 * @author mike
 *
 */
public abstract class Archive
{

  public static final Archive[] fromLibrary(Resource resource)
    throws IOException
  { 
    ArrayList<Archive> archives=new ArrayList<Archive>();
    if (resource.asContainer()!=null)
    { 
      Resource[] children=resource.asContainer().getChildren();
      for (Resource child: children)
      {
        Archive archive=fromResource(child);
        if (archive!=null)
        { archives.add(archive);
        }
      }
      
    }
    return archives.toArray(new Archive[archives.size()]);
  }
  
  public static final Archive fromResource(Resource resource)
  { 
    if (resource.unwrap(FileResource.class) !=null)
    {
      if (resource.getURI().getPath().endsWith(".jar"))
      { return new JarArchive(resource.unwrap(FileResource.class));
      }
      else if (resource.asContainer()!=null)
      { return new FileArchive(resource.unwrap(FileResource.class));
      }
    }
    return null;
  }
  
  
  private final HashMap<String,Entry> entries
    =new HashMap<String,Entry>();
  
  private final HashSet<String> negativePaths
    =new HashSet<String>();
  
  /**
   * Represents a single class or resource in an archive
   * 
   * @author mike
   *
   */
  public abstract class Entry
  {
    public abstract byte[] getData()
      throws IOException;
    
    public abstract URL getResource()
      throws IOException;
    
    public abstract InputStream getResourceAsStream()
      throws IOException;
    
  }
  
  /**
   * 
   * @return An entry, or null if the entry does not exist in this archive
   */
  public final Entry getEntry(String path)
    throws IOException
  {
    Entry entry=entries.get(path);
    if (entry==null)
    { 
      if (!negativePaths.contains(path))
      { entry=loadEntrySync(path);
      }
    }
    return entry;
  }
  
  private final synchronized Entry loadEntrySync(String path)
    throws IOException
  {
    Entry entry=entries.get(path);
    if (entry==null)
    {
      if (!negativePaths.contains(path))
      { entry=loadEntry(path);
      }
      if (entry==null)
      { negativePaths.add(path);
      }
      else
      { entries.put(path, entry);
      }
    }
    return entry;
  }
  
  protected abstract Entry loadEntry(String path)
    throws IOException;
  
  public abstract void open()
    throws IOException;
  
  public void close()
  { 
    entries.clear();
    negativePaths.clear();
  }
  
  protected synchronized void addEntry(String path,Entry entry)
  { entries.put(path,entry);
  }
}
