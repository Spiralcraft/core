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
package spiralcraft.vfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.net.URL;

import spiralcraft.util.Path;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

/**
 * Provides a means to modify the behavior of another Resource.
 * 
 * The subclass must initialize the delegate field with the backing resource.
 * 
 * @author mike
 */
public abstract class ResourceWrapper
  implements Resource
{

  protected abstract Resource getDelegate();
  
  @Override
  public Resource resolve(Path path)
    throws UnresolvableURIException
  { return getDelegate().resolve(path);
  }
  
  @Override
  public Container asContainer()
  { return getDelegate().asContainer();
  }

  @Override
  public Resource[] getChildren()
    throws IOException
  { return getDelegate().getChildren();
  }

  @Override
  public void copyFrom(Resource source) throws IOException
  { getDelegate().copyFrom(source);
  }

  @Override
  public Container ensureContainer() throws IOException
  { return getDelegate().ensureContainer();
  }

  @Override
  public boolean exists() throws IOException
  { return getDelegate().exists();
  }

  @Override
  public InputStream getInputStream() throws IOException
  { return getDelegate().getInputStream();
  }

  @Override
  public long getLastModified() throws IOException
  { return getDelegate().getLastModified();
  }

  @Override
  public String getLocalName()
  { return getDelegate().getLocalName();
  }

  @Override
  public OutputStream getOutputStream() throws IOException
  { return getDelegate().getOutputStream();
  }

  @Override
  public Resource getParent()
    throws IOException
  { return getDelegate().getParent();
  }

  @Override
  public URI getURI()
  { return getDelegate().getURI();
  }

  @Override
  public URI getResolvedURI()
  { return getDelegate().getResolvedURI();
  }
  
  @Override
  public boolean supportsRead()
  { return getDelegate().supportsRead();
  }

  @Override
  public boolean supportsWrite()
  { return getDelegate().supportsWrite();
  }

  @Override
  public long write(InputStream in) throws IOException
  { return getDelegate().write(in);
  }
  
  @Override
  public long getSize()
    throws IOException
  { return getDelegate().getSize();
  }
  
  @Override
  public void renameTo(URI uri)
    throws IOException
  { getDelegate().renameTo(uri);
  }
  
  @Override
  public void delete()
    throws IOException
  { getDelegate().delete();
  }

  @Override
  public void copyTo(Resource target)
    throws IOException
  { getDelegate().copyTo(target);
  }
  
  @Override
  public void moveTo(Resource target)
    throws IOException
  { getDelegate().moveTo(target);
  }
  
  @Override
  public boolean setLastModified(long lastModified)
    throws IOException
  { return getDelegate().setLastModified(lastModified);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T extends Resource> T unwrap(Class<T> clazz)
  {
    if (clazz.isAssignableFrom(this.getClass()))
    { return (T) this;
    }
    return getDelegate().unwrap(clazz);
  }
  
  @Override
  public URL getURL()
  { return getDelegate().getURL();
  }
}
