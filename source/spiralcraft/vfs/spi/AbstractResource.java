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
package spiralcraft.vfs.spi;

import java.net.URI;
import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import spiralcraft.util.Path;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.StreamUtil;

public abstract class AbstractResource
  implements Resource
{
  
  private final URI _uri;

  public AbstractResource(URI uri)
  { _uri=uri;
  }

  public URI getURI()
  { return _uri;
  }

	/**
   * Read not supported, throw an IOException
	 */	
  public InputStream getInputStream()
    throws IOException
  { throw new IOException("Resource "+getURI()+" cannot be read");
  }

  /**
   * Read not supported by default
   */
  public boolean supportsRead()
  { return false;
  }

	/**
   * Throw an IOException, can't write
	 */	
  public OutputStream getOutputStream()
    throws IOException
  { throw new IOException("Resource "+getURI()+" cannot be written");
  }

  /**
   * Doesn't know about parents
   */
  public Resource getParent()
  { return null;
  }

  /**
   * Gets local name from the last element in the URI.
   */
  public String getLocalName()
  { return new Path(_uri.getPath(),'/').lastElement();
  }
  
  /**
   * Write not supported by default
   */
  public boolean supportsWrite()
  { return false;
  }

  public boolean setLastModified(long lastModified)
  { return false;
  }
  
  /**
   * Containership not supported
   */
  public Container asContainer()
  { return null;
  }    

  /**
   * <p>Return the children of this resource, if any.
   * </p>
   * 
   * <p>This method must perform the equivalent of a call to
   *   asContainer().listChildren()
   * </p>
   * 
   * @return
   */
  public Resource[] getChildren()
    throws IOException
  { 
    Container container=asContainer();
    if (container!=null)
    { return container.listChildren();
    }
    else
    { return null;
    }
  }
  
  /**
   * <p>Convenience implementation of Container.listChildren() for Resources 
   *   that also implement Container. 
   * </p>
   * 
   * @return
   * @throws IOException
   */
  public Resource[] listChildren(ResourceFilter filter)
    throws IOException
  { 
    Resource[] children=asContainer().listChildren();
    ArrayList<Resource> buffer=new ArrayList<Resource>(children.length);
    for (Resource resource:children)
    { 
      if (filter.accept(resource))
      { buffer.add(resource);
      }
    }
    return buffer.toArray(new Resource[buffer.size()]);
  }  
  
  public Container ensureContainer()
    throws IOException
  { throw new IOException(getClass().getName()+" does not support containership");
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+_uri.toString();
  }
  
  /**
   * Abstract resources don't exist
   */
  public boolean exists()
    throws IOException
  { return false;
  }
  
  public long getLastModified()
    throws IOException
  { return 0;
  }
  
  /**
   * Default implementation opens an InputStream from
   *   the source and copies bytes read to a new OutputStream.
   *
   * If the InputStream is null, and the source is has a Container
   *   aspect, this resource will ensure that it has a Container
   *   aspect as well.
   */
  public void copyFrom(Resource source)
    throws IOException
  { 
    if (!source.supportsRead())
    { 
      throw new IOException
        ("Resource "+source.getURI()+" does not support read operations");
    }
    if (!supportsWrite())
    { 
      throw new IOException
        ("Resource "+getURI()+" does not support write operations");
    }
    
    InputStream in=null;
    try
    {
      in=source.getInputStream();
      if (in==null)
      { 
        if (source.asContainer()!=null)
        { ensureContainer();
        }
      }
      else
      { write(in);
      }
    }
    finally
    {
      if (in!=null)
      {
        try
        { in.close();
        }
        catch (IOException x)
        { }
      }
    }
  }

  /**
   * Default implementation obtains an OutputStream
   *   and copies the InputStream to it.
   */
  public long write(InputStream in)
    throws IOException
  { 
    if (!supportsWrite())
    { 
      throw new IOException
        ("Resource "+getURI()+" does not support write operations");
    }
    
    OutputStream out=null;
    try
    {
      out=getOutputStream();
      long count=StreamUtil.copyRaw(in,out,8192);
      out.flush();
      return count;
    }
    finally
    {
      if (out!=null)
      {
        try
        { out.close();
        }
        catch (IOException x)
        { }
      }
    }
    
  }
  
  public void moveTo(Resource target)
    throws IOException
  { 
    Container container=target.asContainer();
    if (target.exists() && container!=null)
    { 
      container.getChild(getLocalName()).copyFrom(this);
      this.delete();
    }
    else
    { 
      target.copyFrom(this);
      this.delete();
    }
    
  }  
  
  public void copyTo(Resource target)
    throws IOException
  { 
    Container container=target.asContainer();
    if (target.exists() && container!=null)
    { 
      if (getLocalName()==null)
      { throw new IOException("Cannot copy "+getURI()+" into a container");
      }
      container.getChild(getLocalName()).copyFrom(this);
    }
    else
    { target.copyFrom(this);
    }

  }  
  
  public long getSize()
    throws IOException
  { return 0;
  }
}
