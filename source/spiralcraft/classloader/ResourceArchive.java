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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import spiralcraft.log.ClassLog;
//import spiralcraft.log.Level;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

/**
 * An archive contained in a VFS tree
 * 
 * @author mike
 *
 */
public class ResourceArchive
  extends Archive
{
  
  private static final ClassLog log=ClassLog.getInstance(ResourceArchive.class);
  
  private Resource rootResource;

  
  public ResourceArchive(Resource resource)
  { 
    
//    try
//    { log.fine("Created "+resource.getURI()+" exists="+resource.exists());
//    }
//    catch (IOException x)
//    { log.log(Level.WARNING,"Error accessing "+resource.getURI(),x);
//    }
    if (resource.getURI().getRawPath()!=null
        && !resource.getURI().getRawPath().endsWith("/")
        )
    { 
      try
      {
        rootResource
          =Resolver.getInstance().resolve
            (URIUtil.ensureTrailingSlash(resource.getURI()));
      }
      catch (UnresolvableURIException x)
      { throw new IllegalArgumentException(resource.getURI().toString(),x);
      }
    }
    else
    { this.rootResource=resource;
    }
  }
  
  
  @Override
  public void open()
    throws IOException
  {
    if (!rootResource.exists())
    { 
      log.fine(rootResource+" does not exist");
      throw new IOException("Resource not found "+rootResource.getURI());
    }
    
    if (rootResource.asContainer()==null)
    { throw new IOException("Resource is not a directory "+rootResource.getURI());
    }
    
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+rootResource.getURI();
  }
  
  @Override
  public void close()
  { super.close();
  }

  @Override
  protected Entry loadEntry(String path)
    throws IOException
  {
    try
    {
      Resource resource
        =Resolver.getInstance().resolve
          (rootResource.getURI().resolve(URIUtil.encodeURIPath(path)));
//      log.fine("Checked "+resource.getURI()+"="+resource.exists());
      if (resource.exists())
      { return new ResourceEntry(resource);
      }
      else
      { return null;
      }
    }
    catch (UnresolvableURIException x)
    { 
//      log.log(Level.FINE,"Unresolvable "+rootResource.getURI()+" "+path,x);
      return null;
    }
    catch (IOException x)
    {
//      log.log(Level.FINE,"IOException "+rootResource.getURI()+" "+path,x);
      throw x;
    }

  }

  public class ResourceEntry
    extends Entry
  {
    private Resource resource;
    
    public ResourceEntry(Resource resource)
    { this.resource=resource;
    }

    @Override
    public String toString()
    { return super.toString()+": "+resource.getURI();
    }
    
    @Override
    public byte[] getData()
      throws IOException
    {
      BufferedInputStream in=null;
      try
      {
        in = new BufferedInputStream(resource.getInputStream());

        byte[] data = new byte[(int) resource.getSize()];
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

    @Override
    public URL getResource()
      throws IOException
    { return resource.getURL();
    }

    @Override
    public InputStream getResourceAsStream()
      throws IOException
    { return resource.getInputStream();
    }
    
  }
  
}
