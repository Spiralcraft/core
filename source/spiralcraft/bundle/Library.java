//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Clock;
import spiralcraft.util.ClassLoaderLocal;
import spiralcraft.util.Path;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.file.FileResource;
import spiralcraft.vfs.jar.JarFileResource;

/**
 * A collection of packages located in a vfs container resource
 * 
 * @author mike
 *
 */
public class Library
{
  private static final ClassLoaderLocal<Library> instance
    =new ClassLoaderLocal<Library>();
  
  public static Library get()
  { return instance.getNearestInstance();
  }
  
  public static void set(Library library)
  { 
    if (instance.getContextInstance()==null)
    { 
      library.parent=get();
      instance.setContextInstance(library);
    }
    else
    { 
      throw new IllegalStateException
        ("Library for this classloader already set");
    }
  }
    
  
  private final ClassLog log
    =ClassLog.getInstance(Library.class);
  
  private final Container container;
  
  private HashMap<String,Package> packageNameMap
    =new HashMap<String,Package>();
  private HashSet<Package> packageSet
    =new HashSet<Package>();
  
  private volatile long lastScan;
  private int scanInterval=10000;
  private Library parent;
  
  public Library(Container container)
  { this.container=container;
  }
  
  public Bundle getBundle(String authority)
  {
    scanPackages();
    String[] segments=authority.split("\\.");
    if (segments.length<2)
    { 
      throw new IllegalArgumentException
        ("Missing bundle name in authority '"+authority+"'");
    }
    String packageName=segments[0];
    
    Package pkg=packageNameMap.get(packageName);
    if (pkg!=null)
    { return pkg.getBundle(segments[1]);
    }
    
    if (parent!=null)
    { return parent.getBundle(authority);
    }
    return null;
  }
  
  public Bundle[] getBundles()
  { 
    scanPackages();
    ArrayList<Bundle> bundles=new ArrayList<Bundle>();
    for (Package pkg:packageSet)
    {
      for (Bundle bundle:pkg.getBundles())
      { bundles.add(bundle);
      }
    }
    return bundles.toArray(new Bundle[bundles.size()]);
  }
  
  public Bundle[] getAllBundles()
  { 
    scanPackages();
    ArrayList<Bundle> bundles=new ArrayList<Bundle>();
    if (parent!=null)
    { 
      for (Bundle bundle:parent.getAllBundles())
      { bundles.add(bundle);
      }
    }
    for (Bundle bundle:getBundles())
    { bundles.add(bundle);
    }
    return bundles.toArray(new Bundle[bundles.size()]);
  }
  
  private synchronized void scanPackages()
  {
    if (container==null)
    { return;
    }
    
    // Remove once granular dynamic library updating works 
    if (lastScan>0)
    { return;
    }
    
    if (Clock.instance().approxTimeMillis()-lastScan<scanInterval)
    { return;
    }
    else
    { lastScan=Clock.instance().approxTimeMillis();
    }
    
    Resource[] children;
    try
    { children=container.getChildren();
    }
    catch (IOException x)
    {
      log.log(Level.WARNING,"Error reading library "+container.getURI(),x);
      return;
    }
    
    for (Resource resource:children)
    { 
      if (resource.getLocalName().endsWith(".zip"))
      {
        FileResource fileResource=resource.unwrap(FileResource.class);
        if (fileResource!=null)
        {
        }
        
        Package pkg
          =Package.load
            (new JarFileResource(fileResource.getFile(),Path.ROOT_PATH));
        
        if (pkg!=null)
        { 
          packageSet.add(pkg);
          packageNameMap.put(pkg.getName(),pkg);
        }
      }
    }
    
    
  }

}
