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

import java.net.URI;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.vfs.Container;

/**
 * A set of resources that can be accessed by or mapped into an application 
 * 
 * @author mike
 *
 */
public class Bundle
{
  private static final ClassLog log
    =ClassLog.getInstance(Bundle.class);
  private static Level logLevel
    =ClassLog.getInitialDebugLevel(Bundle.class,Level.INFO);
  
  private final Container container;
  private final Package pkg;
  private final String bundleName;
  private final String authorityName;
  private final URI bundleURI;
  
  public Bundle(Package pkg,Container container)
  { 
    this.pkg=pkg;
    this.container=container;
    
    this.bundleName=container.getLocalName();
    
    this.authorityName
      =pkg.getName()+"."+container.getLocalName();
    
    this.bundleURI=URIPool.create("bundle://"+this.authorityName+"/");
    
    if (logLevel.isDebug())
    {
      log.debug
        ("Loaded bundle "
        +authorityName+" at "
       +container.getURI()
        );
    }
    
  }
  
  public Container getContainer()
  { return container;
  }
  
  public Package getPackage()
  { return pkg;
  }  
  
  public String getAuthorityName()
  { return authorityName;
  }
  
  public String getBundleName()
  { return bundleName;
  }
  
  public URI getBundleURI()
  { return bundleURI;
  }
}
