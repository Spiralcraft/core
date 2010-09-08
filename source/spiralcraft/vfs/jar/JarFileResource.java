//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.vfs.jar;

import spiralcraft.io.InputStreamWrapper;
import spiralcraft.util.Path;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.spi.AbstractResource;

import java.net.URI;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.io.File;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JarFileResource
  extends AbstractResource
  implements Container
{
  private File file;
  private Path path;
  private Resource[] _contents;

  public JarFileResource(File jarFile,Path path)
  { 
    super(URI.create("jar:"+jarFile.toURI()+"!/"+path));
    this.file=jarFile;
    this.path=path;
    
  }
  
  public JarFileResource(URI uri)
  { 
    super(uri);
    String[] uriParts=uri.getSchemeSpecificPart().split("!");
    
    this.file=new File(URI.create(uriParts[0]));
    this.path=new Path(uriParts[1].substring(1),'/');
  }

  @Override
  public Resource asResource()
  { return this;
  }
  
  
  @Override
  public InputStream getInputStream()
    throws IOException
  { 
    final JarFile jar=new JarFile(file);
    
    JarEntry entry=jar.getJarEntry(path.toString());
    
    return new InputStreamWrapper(jar.getInputStream(entry))
    {
      @Override
      public void close()
        throws IOException
      { 
        try
        { super.close();
        }
        finally
        { jar.close();
        }
      } 
    };
  }

  @Override
  public boolean supportsRead()
  { return true;
  }

  @Override
  public OutputStream getOutputStream()
    throws IOException
  { 
    throw new UnsupportedOperationException
      ("Cannot write to an entry in a jar file");

  }

  @Override
  public boolean supportsWrite()
  { return false;
  }

  @Override
  public Container asContainer()
  { 
    if (_contents==null)
    {
      try
      { makeContents();
      }
      catch (IOException x)
      { }
    }
    
    if (_contents!=null)
    { return this;
    }
    return null;
  }

  @Override
  public Container ensureContainer()
    throws IOException
  {
    Container container=asContainer();
    if (container==null)
    { throw new IOException("Cannot create directories in a jar file");
    }
    return container;    
  }
  
  @Override
  public Resource getParent()
  { return new JarFileResource(file,path.parentPath());
  }

  @Override
  public String getLocalName()
  { return path.lastElement();
  }
  
  @Override
  public long getLastModified()
    throws IOException
  { 
    final JarFile jar=new JarFile(file);
    try
    { 
      JarEntry entry=jar.getJarEntry(path.toString());
      return entry.getTime();
    }
    finally
    { jar.close();
    }

  }
  
  @Override
  public Resource[] listContents()
    throws IOException
  { 
    makeContents();
    return _contents;
  }

  @Override
  public Resource[] listChildren()
    throws IOException
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
    return new JarFileResource(file,path.append(name));
  }

  @Override
  public Resource createLink(String name,Resource resource)
    throws UnresolvableURIException
  { throw new UnsupportedOperationException();
  }

  private void makeContents()
    throws IOException
  { 
    final JarFile jar=new JarFile(file);
    try
    { 
      Enumeration<JarEntry> entries=jar.entries();
      String pathString=path.format("/");
      if (!pathString.endsWith("/"))
      { pathString=pathString+"/";
      }
      List<Resource> children=new ArrayList<Resource>();
      while (entries.hasMoreElements())
      {
        JarEntry entry=entries.nextElement();
        if (entry.getName().startsWith(pathString)
            && entry.getName().length()>pathString.length()
            )
        { 
          int slash=entry.getName().indexOf('/',pathString.length());
          if (slash<0 || slash==entry.getName().length()-1)
          { 
            // Only add immediate children
            children.add
              (new JarFileResource(file,new Path(entry.getName(),'/')));
          }
        }
      }
      
      if (children.size()>0)
      { _contents=children.toArray(new Resource[children.size()]);
      }
      else
      { _contents=null;
      }
      
    }
    finally
    { 
      try
      { jar.close();
      }
      catch (IOException x)
      { }
    }


  }
  
  @Override
  public boolean exists()
    throws IOException
  { 
    final JarFile jar=new JarFile(file);
    try
    { 
      JarEntry entry=jar.getJarEntry(path.toString());
      return entry!=null;
    }
    finally
    { jar.close();
    }

  }
  
  @Override
  public long getSize()
    throws IOException
  { 
    final JarFile jar=new JarFile(file);
    try
    { 
      JarEntry entry=jar.getJarEntry(path.toString());
      return entry.getSize();
    }
    finally
    { jar.close();
    }
  }
  
  @Override
  public void renameTo(URI uri)
    throws IOException
  { throw new UnsupportedOperationException("Cannot rename a jar entry");
  }
  
  @Override
  public void moveTo(Resource target)
    throws IOException
  { throw new UnsupportedOperationException("Cannot move a jar entry");
  }      
  
  @Override
  public void delete()
  { throw new UnsupportedOperationException("Cannot delete a jar entry");
  }
}
