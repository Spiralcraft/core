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

import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;

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
  
  public Container asContainer()
  { return getDelegate().asContainer();
  }

  public Resource[] getChildren()
    throws IOException
  { return getDelegate().getChildren();
  }

  public void copyFrom(Resource source) throws IOException
  { getDelegate().copyFrom(source);
  }

  public Container ensureContainer() throws IOException
  { return getDelegate().ensureContainer();
  }

  public boolean exists() throws IOException
  { return getDelegate().exists();
  }

  public InputStream getInputStream() throws IOException
  { return getDelegate().getInputStream();
  }

  public long getLastModified() throws IOException
  { return getDelegate().getLastModified();
  }

  public String getLocalName()
  { return getDelegate().getLocalName();
  }

  public OutputStream getOutputStream() throws IOException
  { return getDelegate().getOutputStream();
  }

  public Resource getParent()
    throws IOException
  { return getDelegate().getParent();
  }

  public URI getURI()
  { return getDelegate().getURI();
  }

  public URI getResolvedURI()
  { return getDelegate().getResolvedURI();
  }
  
  public boolean supportsRead()
  { return getDelegate().supportsRead();
  }

  public boolean supportsWrite()
  { return getDelegate().supportsWrite();
  }

  public long write(InputStream in) throws IOException
  { return getDelegate().write(in);
  }
  
  public long getSize()
    throws IOException
  { return getDelegate().getSize();
  }
  
  public void renameTo(URI uri)
    throws IOException
  { getDelegate().renameTo(uri);
  }
  
  public void delete()
    throws IOException
  { getDelegate().delete();
  }

  public void copyTo(Resource target)
    throws IOException
  { getDelegate().copyTo(target);
  }
  
  public void moveTo(Resource target)
    throws IOException
  { getDelegate().moveTo(target);
  }
  
  public boolean setLastModified(long lastModified)
    throws IOException
  { return getDelegate().setLastModified(lastModified);
  }
}
