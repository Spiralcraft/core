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
package spiralcraft.stream.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;

import spiralcraft.stream.Container;
import spiralcraft.stream.Resource;

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

  protected Resource delegate;
  
  public Container asContainer()
  { return delegate.asContainer();
  }

  public void copyFrom(Resource source) throws IOException
  { delegate.copyFrom(source);
  }

  public Container ensureContainer() throws IOException
  { return delegate.ensureContainer();
  }

  public boolean exists() throws IOException
  { return delegate.exists();
  }

  public InputStream getInputStream() throws IOException
  { return delegate.getInputStream();
  }

  public long getLastModified() throws IOException
  { return delegate.getLastModified();
  }

  public String getLocalName()
  { return delegate.getLocalName();
  }

  public OutputStream getOutputStream() throws IOException
  { return delegate.getOutputStream();
  }

  public Resource getParent()
  { return delegate.getParent();
  }

  public URI getURI()
  { return delegate.getURI();
  }

  public boolean supportsRead()
  { return delegate.supportsRead();
  }

  public boolean supportsWrite()
  { return delegate.supportsWrite();
  }

  public long write(InputStream in) throws IOException
  { return delegate.write(in);
  }

}
