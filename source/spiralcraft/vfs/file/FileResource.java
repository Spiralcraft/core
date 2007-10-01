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
package spiralcraft.vfs.file;

import spiralcraft.vfs.AbstractResource;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.NotStreamableException;

import java.net.URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileResource
  extends AbstractResource
  implements Container
{
  private File _file;
  private Resource[] _contents;

  public FileResource(File file)
  { 
    super(file.toURI());
    _file=file;
  }
  
  public FileResource(URI uri)
    throws UnresolvableURIException
  { 
    super(uri);
    _file=new File(uri);
  }

  public InputStream getInputStream()
    throws IOException
  { 
    if (_file.isDirectory())
    { 
      throw new NotStreamableException
        (getURI(),"Directory is not streamable");
    }
    return new FileInputStream(_file);
  }

  public boolean supportsRead()
  { return true;
  }

  public OutputStream getOutputStream()
    throws IOException
  { 
    if (_file.isDirectory())
    { return null;
    }
    return new FileOutputStream(_file);
  }

  public boolean supportsWrite()
  { return true;
  }

  public Container asContainer()
  { 
    if (_file.isDirectory())
    { return this;
    }
    return null;
  }

  public Container ensureContainer()
    throws IOException
  {
    if (!_file.exists())
    { 
      if (_file.mkdir())
      { return this;
      }
      else
      { throw new IOException("Could not create directory "+_file.getPath());
      }
    }
    else
    {
      if (_file.isDirectory())
      { return this;
      }
      else
      { throw new IOException("File "+_file.getPath()+" is not a directory");
      }
    }
    
  }
  
  public Resource getParent()
  { 
    try
    { return new FileResource(_file.getParentFile().toURI());
    }
    catch (UnresolvableURIException x)
    { 
      x.printStackTrace();
      return null;
    }
  }

  public String getLocalName()
  { return _file.getName();
  }
  
  public long getLastModified()
  { return _file.lastModified();
  }
  
  public Resource[] listContents()
  { 
    makeContents();
    return _contents;
  }

  public Resource[] listChildren()
  {
    makeContents();
    return _contents;
  }

  public Resource[] listLinks()
  { 
    // We don't know how to determine symbolic link
    return null;
  }

  public Resource getChild(String name)
    throws UnresolvableURIException
  { return new FileResource(new File(_file,name).toURI());
  }

  public Resource createLink(String name,Resource resource)
    throws UnresolvableURIException
  { throw new UnsupportedOperationException();
  }

  private void makeContents()
  { 
    try
    {
      File[] contents=_file.listFiles();
      if (contents!=null)
      {
        _contents=new Resource[contents.length];
        for (int i=0;i<contents.length;i++)
        { _contents[i]=new FileResource(contents[i].toURI());
        }
      }
      else
      { _contents=null;
      }
    }
    catch (UnresolvableURIException x)
    { x.printStackTrace();
    }
  }
  
  public boolean exists()
  { return _file.exists();
  }
}
