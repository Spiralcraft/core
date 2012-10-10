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

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.IteratorEnumeration;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.file.FileResource;

public class Loader
  extends ClassLoader
  implements Lifecycle
{
  private static final ClassLog log
    =ClassLog.getInstance(Loader.class);

  private Level logLevel
    =ClassLog.getInitialDebugLevel(Loader.class,Level.INFO);
//    =Level.FINE;
  
  private final ArrayList<Archive> archives=new ArrayList<Archive>();
  private final ArrayList<Archive> precedentArchives=new ArrayList<Archive>();
  private boolean started;
  private boolean debug;
  private ClassLoader contextClassLoader
    =Thread.currentThread().getContextClassLoader();
  
  public Loader()
  {
  }
  
  public Loader(ClassLoader parent)
  { super(parent);
  }
  
  public Loader(ClassLoader parent,Resource[] classPath)
  {
    super(parent);
    if (classPath!=null)
    {
      for (Resource resource:classPath)
      {
        FileResource fileResource=resource.unwrap(FileResource.class);
        if (fileResource!=null)
        {
          if (fileResource.asContainer()==null)
          { 
            if (logLevel.isFine())
            { log.fine("Adding jar classpath "+fileResource.getURI());
            }
            addArchive(new JarArchive(fileResource));
          }
          else
          { 
            if (logLevel.isFine())
            { log.fine("Adding file classpath "+fileResource.getURI());
            }
            addArchive(new ResourceArchive(fileResource));
          }
        }
        else
        { 
          if (resource.asContainer()!=null)
          { 
            if (logLevel.isFine())
            { log.fine("Adding resource classpath "+resource.getURI());
            }
            addArchive(new ResourceArchive(resource));
          }
          else
          {          
            throw new UnsupportedOperationException
              ("Resource "+resource.getURI()
                +" is not recognized as a class archive by this classloader"
              ); 
          }
        }
      }
    }
  }
  
  public void setDebug(boolean debug)
  { 
    this.debug=debug;
    if (debug && !logLevel.isDebug())
    { logLevel=Level.DEBUG;
    }
    
    if (!debug && logLevel.isDebug())
    { logLevel=Level.INFO;
    }
  }
  
  public void setLogLevel(Level level)
  { this.logLevel=level;
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
    if (logLevel.isTrace())
    { log.trace(formalName);
    }
    Class<?> clazz=findLoadedClass(formalName);
    if (clazz==null)
    { clazz=findClass(formalName,precedentArchives);
    }
    if (clazz==null)
    { 
      try
      {
        if (getParent()!=null)
        { clazz=getParent().loadClass(formalName);
        }
        else
        { clazz=ClassLoader.getSystemClassLoader().loadClass(formalName);
        }
      }
      catch (ClassNotFoundException x)
      {
      }
    }
    if (clazz==null)
    { clazz=findClass(formalName);
    }
    if (debug || logLevel.isFine())
    { log.fine( (clazz!=null?"FOUND":"FAIL")+":"+formalName );
    }
    if (resolve && clazz!=null)
    { this.resolveClass(clazz);
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
  protected Class<?> findClass(String formalName)
    throws ClassNotFoundException
  { 
    Class<?> clazz=findClass(formalName,archives);
    if (clazz==null)
    { throw new ClassNotFoundException(formalName);
    }
    return clazz;
  }
  
  private ClassLoader pushClassLoader()
  { 
    ClassLoader oldCl=Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(contextClassLoader);
    return oldCl;
  }
  
  private void popClassLoader(ClassLoader oldCl)
  { Thread.currentThread().setContextClassLoader(oldCl);
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
  private Class<?> findClass(String formalName,ArrayList<Archive> archives)
  { 
    ClassLoader oldCl=pushClassLoader();
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
    finally
    { popClassLoader(oldCl);
    }
  }

  private URL findResource(String path,ArrayList<Archive> archives)
  {
    ClassLoader oldCl=pushClassLoader();
    try
    {
      Archive.Entry entry=findEntry(path,archives);
      if (entry==null)
      { 
        if (debug || logLevel.isFine())
        { log.fine( "FAIL:"+path );
        }
        return null;
      }
      else
      { 
        if (debug || logLevel.isFine())
        { log.fine( "FOUND:"+path );
        }
        return entry.getResource();
      }
    }
    catch (IOException x)
    { 
      x.printStackTrace();
      return null;
    }
    finally
    { popClassLoader(oldCl);
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
    if (logLevel.isTrace())
    { log.trace(path);
    }
    URL resource=findPrecedentResource(path);
    if (resource==null)
    { 
      if (getParent()!=null)
      { resource=getParent().getResource(path);
      }
      else
      { resource=ClassLoader.getSystemClassLoader().getResource(path);
      }
    }
    if (resource==null)
    { resource=findResource(path);
    }
    if (debug)
    { log.fine( (resource!=null?"FOUND":"FAIL")+":"+path );
    }
    return resource;
  }

  @Override
  public Enumeration<URL> getResources(String path)
    throws IOException
  {
    ClassLoader oldCl=pushClassLoader();
    try
    {
      if (logLevel.isTrace())
      { log.trace(path);
      }
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
      else
      {
        Enumeration<URL> renum=ClassLoader.getSystemClassLoader().getResources(path);
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
    finally
    { popClassLoader(oldCl);
    }
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
    ClassLoader cl=pushClassLoader();
    try
    {
      if (logLevel.isTrace())
      { log.trace(path);
      }
      InputStream in=findPrecedentStream(path);
      if (in==null)
      { 
        if (getParent()!=null)
        { in=getParent().getResourceAsStream(path);
        }
        else
        { in=ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        }
        
      }
      
      if (in==null)
      { in=findStream(path);
      }
      if (debug || logLevel.isFine())
      { log.fine( (in!=null?"FOUND":"FAIL")+":"+path );
      }
      
      return in;
    }
    finally
    { popClassLoader(cl);
    }
    
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
      else
      { 
        if (logLevel.isFine())
        { log.fine("Path "+path+" not found in "+archive.toString());
        }
        
      }
    }
    return null;
  }

  @Override
  public void start()
    throws LifecycleException
  {
    if (debug && logLevel.isTrace())
    { log.trace("starting...");
    }
    
    try
    {
      for (Archive archive: archives)
      { 
        if (debug || logLevel.isDebug())
        { log.trace("Opening local archive "+archive);
        }
        archive.open();
      }
    
      for (Archive archive: precedentArchives)
      { 
        if (debug || logLevel.isDebug())
        { log.trace("Opening precedent archive "+archive);
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
