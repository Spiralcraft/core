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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Container;

/**
 * A collection of bundles packaged within a distribution resource
 *   (an archive file or a directory tree)
 * 
 * @author mike
 *
 */
public class Package
{
  private static final ClassLog log
    =ClassLog.getInstance(Package.class);
  
  private final Resource resource;
  private Properties properties;
  private String name;
  private HashMap<String,Bundle> bundleNameMap
    =new HashMap<String,Bundle>();
  
  /**
   * Determine if the specified resource is the root of a Package and
   *   return the associated Package object. 
   * 
   * @param resource
   * @return The package, or null if the resource is not the root of a package
   */
  public static Package load(Resource resource)
  { 
    try
    { return new Package(resource);
    }
    catch (BundleException x)
    { 
      log.log(Level.WARNING,"Error loading package",x);
      return null;
    }
  }
  
  public Package(Resource resource)
    throws BundleException
  { 
    this.resource=resource;
    loadProperties();
    loadBundles();
    
    if (name==null)
    { 
      throw new BundleException
        ("No package name defined for package root "+resource.getURI());
    }
    log.fine("Loaded package '"+name+"' from "+resource.getURI());
  }
 
  public String getName()
  { return name;
  }
  
  public Bundle getBundle(String name)
  { return bundleNameMap.get(name);
  }
  
  public Bundle[] getBundles()
  { 
    return bundleNameMap.values().toArray(new Bundle[bundleNameMap.size()]);
  }
  
  private void loadBundles()
  {
    try
    {
      for (Resource child : resource.getChildren())
      { 
        if (!child.getLocalName().equals("META-INF")
             && child.asContainer()!=null
           )
        { 
          bundleNameMap.put
            (child.getLocalName()
            ,new Bundle(this,child.asContainer())
            );
        }
     
      }
    }
    catch (IOException x)
    { log.warning("Error reading bundles from package in "+resource.getURI());
    }
  }
  
  private void loadProperties()
    throws BundleException
  {
    properties=new Properties();
    
    try
    { 
      Container packageContainer
        =resource.asContainer();
      if (packageContainer==null)
      { throw new BundleException("Not a container "+resource.getURI());
      }
      Container metaContainer
        =packageContainer.getChild("META-INF").asContainer();
      if (metaContainer!=null)
      {
        Resource propertiesResource
          =metaContainer.getChild("Package.properties");
        if (propertiesResource.exists())
        { 
          InputStream in=propertiesResource.getInputStream();
          try
          { properties.load(in);
          }
          finally
          { in.close();
          }
          name=properties.getProperty("package.name");
        }
      }
    }
    catch (IOException x)
    { 
      log.log
        (Level.WARNING
        ,"Error reading META-INF/Package.properties in "+resource.getURI()
        ,x
        );
    }  
    
    
  }
  
}
