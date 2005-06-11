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
package spiralcraft.stream;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
   * Write not supported by default
   */
  public boolean supportsWrite()
  { return false;
  }

  /**
   * Containership not supported
   */
  public Container asContainer()
  { return null;
  }    

  public Container ensureContainer()
    throws IOException
  { throw new IOException(getClass().getName()+" does not support containership");
  }
  
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
}
