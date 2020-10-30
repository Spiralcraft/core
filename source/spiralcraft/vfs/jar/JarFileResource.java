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

//import spiralcraft.log.ClassLog;
import spiralcraft.util.Path;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.spi.AbstractResource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JarFileResource
  extends AbstractResource
  implements Container
{
//  private static final ClassLog log
//    =ClassLog.getInstance(JarFileResource.class);
      
  private File file;
  private Path path;
  private Resource[] _contents;
  private final JarCache jarCache;

  public JarFileResource(File jarFile,Path path)
  { 
    super
      (URIPool.create
        ("jar:"
        +jarFile.toURI()+"!"
        +URIUtil.encodeURIPath(path.isAbsolute()?path.toString():("/"+path))
        )
      );
    this.file=jarFile;
    this.path=path.subPath(0);
    this.jarCache=JarCache.get(file);
    
  }
  
  public JarFileResource(URI uri)
  { 
    super(uri);
    String[] uriParts=uri.getRawSchemeSpecificPart().split("!");
    
    this.file=new File(URIPool.create(uriParts[0]));
    this.path=new Path(URIUtil.decodeURIPath(uriParts[1].substring(1)),'/');
    this.jarCache=JarCache.get(file);
  }
  
  @Override
  public Resource resolve(Path childPath)
  { 
    if (childPath.isAbsolute())
    { return new JarFileResource(file,childPath);
    }
    else
    { return new JarFileResource(file,path.append(childPath));
    }
  }
  
  @Override
  public InputStream getInputStream()
    throws IOException
  { 
    JarEntry entry=jarCache.getJarEntry(path.toString());
    return jarCache.getInputStream(entry);
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
  { 
    if (path.parentPath()!=null)
    { return new JarFileResource(file,path.parentPath());
    }
    else
    { return null;
    }
  }

  @Override
  public String getLocalName()
  { return path.lastElement();
  }
  
  @Override
  public long getLastModified()
    throws IOException
  { 
    JarEntry entry=jarCache.getJarEntry(path.toString());
    return entry.getTime();
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
    Enumeration<JarEntry> entries=jarCache.entries();
    String pathString=path.format("/");
    if (!pathString.endsWith("/"))
    { 
      if (pathString.length()>0)
      { pathString=pathString+"/";
      }
    }
    else
    { 
      if (pathString.length()==1)
      { pathString="";
      }
    }
    List<Resource> children=new ArrayList<Resource>();
    while (entries.hasMoreElements())
    {
      JarEntry entry=entries.nextElement();
      String entryName=entry.getName();
      if (entryName.startsWith(pathString)
          && entryName.length()>pathString.length()
          )
      { 
        int slash=entryName.indexOf('/',pathString.length());
        if (slash<0 || slash==entryName.length()-1)
        { 
          //log.fine("Added "+entry.getName());
          // Only add immediate children
          children.add
            (new JarFileResource(file,new Path(entryName,'/')));
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
  
  @Override
  public boolean exists()
    throws IOException
  { 
    JarEntry entry=jarCache.getJarEntry(path.toString());
    return entry!=null;
  }
  
  @Override
  public long getSize()
    throws IOException
  { 
    JarEntry entry=jarCache.getJarEntry(path.toString());
    return entry.getSize();
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
