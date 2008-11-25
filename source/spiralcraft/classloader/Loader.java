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
import java.util.Enumeration;
import java.util.LinkedList;

import spiralcraft.builder.Lifecycle;
import spiralcraft.builder.LifecycleException;

import spiralcraft.log.ClassLog;
import spiralcraft.util.IteratorEnumeration;

public class Loader
  extends ClassLoader
  implements Lifecycle
{
  private static final ClassLog log
    =ClassLog.getInstance(Loader.class);
  
  private final ArrayList<Archive> archives=new ArrayList<Archive>();
  private final ArrayList<Archive> precedentArchives=new ArrayList<Archive>();
  private boolean started;
  private boolean debug;
  
  public Loader()
  {
  }
  
  public Loader(ClassLoader parent)
  { super(parent);
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * Add an archive to the search path for this ClassLoader within the
   *   standard delegation model (checked after parent ClassLoaders are checked)
   * 
   * @param archive
   */
  public void addArchive(Archive archive)
  { 
    assertNotStarted();
    archives.add(archive);
  }
  
  /**
   * Add an archive to the search path for this ClassLoader 
   *   to be checked BEFORE parent ClassLoaders are checked.
   * 
   * @param archive
   */
  public void addPrecedentArchive(Archive archive)
  { 
    assertNotStarted();
    precedentArchives.add(archive);
  }

  @Override
  protected Class<?> loadClass(String formalName,boolean resolve)
    throws ClassNotFoundException
  {
    Class<?> clazz=findLoadedClass(formalName);
    if (clazz==null)
    { clazz=findClass(formalName,precedentArchives);
    }
    if (clazz==null && getParent()!=null)
    { clazz=getParent().loadClass(formalName);
    }
    if (clazz==null)
    { clazz=findClass(formalName);
    }
    return clazz;
    
  }

  /**
   * Find a class by formal name (a.b.name), within the normal delegation
   *   model (ie. throw an ClassNotFoundException if not found).
   * 
   *@return The class
   *
   *@throws ClassNotFoundException if the class was not loadable by this
   *  ClassLoader.
   */
  @Override
  public Class<?> findClass(String formalName)
    throws ClassNotFoundException
  { 
    Class<?> clazz=findClass(formalName,archives);
    if (clazz==null)
    { throw new ClassNotFoundException(formalName);
    }
    return clazz;
  }
  
  /**
   * Find a class by formal name (a.b.name), within the normal delegation
   *   model (ie. throw an ClassNotFoundException if not found).
   * 
   *@return The class
   *
   *@throws ClassNotFoundException if the class was not loadable by this
   *  ClassLoader.
   */
  public Class<?> findClass(String formalName,ArrayList<Archive> archives)
  { 
    try
    {
      String path = formalName.replace('.', '/')+".class";
      Archive.Entry entry=findEntry(path,archives);
      if (entry==null)
      { return null;
      }
      else
      { 
        byte[] data=entry.getData();
        return defineClass(formalName,data,0,data.length);
      }
    }
    catch (IOException x)
    { 
      x.printStackTrace();
      return null;
    }    
  }

  private URL findResource(String path,ArrayList<Archive> archives)
  {
    try
    {
      Archive.Entry entry=findEntry(path,archives);
      if (entry==null)
      { return null;
      }
      else
      { return entry.getResource();
      }
    }
    catch (IOException x)
    { 
      x.printStackTrace();
      return null;
    }
  }

  /**
   * Find a resource by path (x/y/z) within the normal delegation model
   *   (assumes parent classloaders and precedent archives have been checked)
   */
  @Override
  public URL findResource(String path)
  { return findResource(path,archives);
  }

  private URL findPrecedentResource(String path)
  { return findResource(path,precedentArchives);
  }
  
  @Override
  public URL getResource(String path)
  {
    URL resource=findPrecedentResource(path);
    if (resource==null && getParent()!=null)
    { resource=getParent().getResource(path);
    }
    if (resource==null)
    { resource=findResource(path);
    }
    return resource;
  }

  @Override
  public Enumeration<URL> getResources(String path)
    throws IOException
  {
    LinkedList<URL> resources=new LinkedList<URL>();
    for (Archive archive: precedentArchives)
    {
      try
      {
        Archive.Entry entry=archive.getEntry(path);
        if (entry!=null)
        { resources.add(entry.getResource());
        }
      }
      catch (IOException x)
      { x.printStackTrace();
      }
    }
    
    if (getParent()!=null)
    {
      Enumeration<URL> renum=getParent().getResources(path);
      if (renum!=null)
      {
        while (renum.hasMoreElements())
        { resources.add(renum.nextElement());
        }
      }
    }
        
    for (Archive archive: archives)
    {
      try
      {
        Archive.Entry entry=archive.getEntry(path);
        if (entry!=null)
        { resources.add(entry.getResource());
        }
      }
      catch (IOException x)
      { x.printStackTrace();
      }
    }
    
    return new IteratorEnumeration<URL>(resources.iterator());
  }
  
  private InputStream findStream(String path,ArrayList<Archive> archives)
  {
    try
    {
      Archive.Entry entry=findEntry(path,archives);
      if (entry==null)
      { return null;
      }
      else
      { return entry.getResourceAsStream();
      }
    }
    catch (IOException x)
    { 
      x.printStackTrace();
      return null;
    }
  }
  
  private InputStream findStream(String path)
  { return findStream(path,archives);
  }

  private InputStream findPrecedentStream(String path)
  { return findStream(path,precedentArchives);
  }

  @Override
  public InputStream getResourceAsStream(String path)
  {
    InputStream in=findPrecedentStream(path);
    if (in==null && getParent()!=null)
    { in=getParent().getResourceAsStream(path);
    }
    if (in==null)
    { in=findStream(path);
    }
    return in;
    
  } 
  
  /**
   * Find an entry with the specified path in the specified set of archives
   *
   */
  private Archive.Entry findEntry(String path,ArrayList<Archive> archives)
    throws IOException
  {
    for (Archive archive : archives)
    { 
      Archive.Entry entry=archive.getEntry(path);
      if (entry!=null)
      { 
        if (debug)
        { log.fine("Found entry "+entry.toString()+" in "+archive.toString());
        }
        return entry;
      }
    }
    return null;
  }

  @Override
  public void start()
    throws LifecycleException
  {
    if (debug)
    { log.fine("starting...");
    }
    
    try
    {
      for (Archive archive: archives)
      { 
        if (debug)
        { log.fine("Opening local archive "+archive);
        }
        archive.open();
      }
    
      for (Archive archive: precedentArchives)
      { 
        if (debug)
        { log.fine("Opening precedent archive "+archive);
        }
        archive.open();
      }
      started=true;
    }
    catch (IOException x)
    {
      try
      { stop();
      }
      catch (LifecycleException y)
      { y.printStackTrace();
      }
      throw new LifecycleException("Error starting ClassLoader",x);
    }
    
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    for (Archive archive: archives)
    { archive.close();
    }
    
    for (Archive archive: precedentArchives)
    { archive.close();
    }
    
  }
  
  private void assertNotStarted()
  {
    if (started)
    { 
      throw new IllegalStateException
        ("Operation cannot be performed while ClassLoader is running");
    }
  }
  
  
}
