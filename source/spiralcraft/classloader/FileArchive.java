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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.file.FileResource;

/**
 * An archive contained in a directory tree
 * 
 * @author mike
 *
 */
public class FileArchive
  extends Archive
{
  
  private FileResource resource;
  
  public FileArchive(File file)
    throws IOException
  { this(new FileResource(file));
  }
  
  public FileArchive(FileResource resource)
    throws IOException
  { this.resource=resource;
  }
  
  
  public void open()
    throws IOException
  {
    if (!resource.exists())
    { throw new IOException("Resource not found "+resource.getURI());
    }
    
    if (resource.asContainer()==null)
    { throw new IOException("Resource is not a directory "+resource.getURI());
    }
    
  }
  
  public void close()
  { super.close();
  }

  protected Entry loadEntry(String path)
    throws IOException
  {
    FileResource fileResource
      =(FileResource) Resolver.getInstance().resolve
        (resource.getURI().resolve(path));
    
    if (fileResource.exists())
    { return new FileEntry(fileResource);
    }
    else
    { return null;
    }
  }

  public class FileEntry
    extends Entry
  {
    private FileResource resource;
    
    public FileEntry(FileResource resource)
    { this.resource=resource;
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
    { return resource.getURI().toURL();
    }

    @Override
    public InputStream getResourceAsStream()
      throws IOException
    { return resource.getInputStream();
    }
    
  }
  
}
