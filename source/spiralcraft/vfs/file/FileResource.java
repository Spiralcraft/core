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

import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.NotStreamableException;
import spiralcraft.vfs.spi.AbstractResource;

import java.net.URI;
import java.net.URISyntaxException;

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
  
  public static URI toRelativeURI(File file)
  { 
    try
    {
      return new URI
        (null
        ,file.getPath().replace(File.separatorChar,'/')
        ,null
        );
    }
    catch (URISyntaxException x)
    { throw new RuntimeException(x);
    }
  }
  
  private File _file;
  private Resource[] _contents;

  
  public FileResource(File file)
  { 
    super
      (file.toURI()
      ,file.isAbsolute()
      ?file.toURI()
      :toRelativeURI(file)
      );
    _file=file;
  }
  
  public FileResource(URI uri)
  { 
    super(uri,uri);
    _file=new File(uri);
  }

  @Override
  @Deprecated
  public Resource asResource()
  { return this;
  }
  
  public File getFile()
  { return _file;
  }
  
  @Override
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

  @Override
  public boolean supportsRead()
  { return true;
  }

  @Override
  public OutputStream getOutputStream()
    throws IOException
  { 
    if (_file.isDirectory())
    { return null;
    }
    return new FileOutputStream(_file);
  }

  @Override
  public boolean supportsWrite()
  { return true;
  }

  @Override
  public Container asContainer()
  { 
    if (_file.isDirectory())
    { return this;
    }
    return null;
  }

  @Override
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
  
  @Override
  public Resource getParent()
    throws IOException
  { return new FileResource(_file.getParentFile());
  }

  @Override
  public String getLocalName()
  { return _file.getName();
  }
  
  @Override
  public long getLastModified()
  { return _file.lastModified();
  }
  
  @Override
  public boolean setLastModified(long lastModified)
  { return _file.setLastModified(lastModified);
  }
  
  @Override
  public Resource[] listContents()
    throws UnresolvableURIException  
  { 
    makeContents();
    return _contents;
  }

  @Override
  public Resource[] listChildren()
    throws UnresolvableURIException
  {
    makeContents();
    return _contents;
  }

  @Override
  public Resource[] listLinks()
  { 
    // We don't know how to determine symbolic link
    return null;
  }

  @Override
  public Resource getChild(String name)
    throws UnresolvableURIException
  { 
    if (name==null)
    { throw new NullPointerException("Child name cannot be null");
    }
    return new FileResource(new File(_file,name));
  }

  @Override
  public Resource createLink(String name,Resource resource)
    throws UnresolvableURIException
  { throw new UnsupportedOperationException();
  }

  private void makeContents()
    throws UnresolvableURIException
  { 
    File[] contents=_file.listFiles();
    if (contents!=null)
    {
      _contents=new Resource[contents.length];
      for (int i=0;i<contents.length;i++)
      { _contents[i]=new FileResource(contents[i]);
      }
    }
    else
    { _contents=null;
    }
  }
  
  @Override
  public boolean exists()
  { return _file.exists();
  }
  
  @Override
  public long getSize()
  { return _file.length();
  }
  
  @Override
  public void renameTo(URI uri)
    throws IOException
  { 
    if (!_file.renameTo(new File(uri)))
    { throw new IOException("Rename failed: '"+_file.toURI()+"' to '"+uri+"'");
    }
  }
  
  @Override
  public void moveTo(Resource target)
    throws IOException
  { 
    if (!(target instanceof FileResource))
    { 
      super.moveTo(target);
      return;
    }
    
    Container container=target.asContainer();
    if (target.exists() && container!=null)
    { 
      if (!_file.renameTo
          ( ((FileResource) container.getChild(getLocalName()))
          .getFile()
          ))
      { super.moveTo(target);
      }
      else
      { this.delete();
      }
    }
    else
    { 
      if (!_file.renameTo
          ( ((FileResource) target)
          .getFile()
          ))
      { super.moveTo(target);
      }
      else
      { this.delete();
      }
    }
    
  }      
  
  @Override
  public void delete()
  { _file.delete();
  }
}
