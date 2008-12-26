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
package spiralcraft.vfs.classpath;

import spiralcraft.vfs.AbstractResource;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClasspathResource
  extends AbstractResource
{
  private final ClassLoader _classLoader;
  private final String _path;
  private URL _url;

  public ClasspathResource(URI uri)
  { 
    super(uri);
    _path=uri.getPath().substring(1);
    _classLoader=Thread.currentThread().getContextClassLoader();
  }

  @Override
  public InputStream getInputStream()
    throws IOException
  { return _classLoader.getResourceAsStream(_path);
  }

  @Override
  public boolean supportsRead()
  { return true;
  }

  @Override
  public OutputStream getOutputStream()
    throws IOException
  { 
    if (_url==null)
    { _url=_classLoader.getResource(_path);
    }
    if (_url==null)
    { throw new IOException("Resource '"+_path+"' cannot be written to");
    }
    URLConnection connection=_url.openConnection();
    connection.setDoOutput(true);
    return connection.getOutputStream();
  }

  @Override
  public boolean supportsWrite()
  { return true;
  }
  
  public void renameTo(URI name)
  { 
    throw new UnsupportedOperationException
      ("A classpath resource cannot be renamed");
  }  

  @Override
  public boolean exists()
    throws IOException
  {
    InputStream in=_classLoader.getResourceAsStream(_path);
    if (in!=null)
    { 
      in.close();
      return true;
    }
    return false; 
  }
  
  public void delete()
    throws IOException
  { throw new IOException("ClasspathResource is read-only");
  }
}
