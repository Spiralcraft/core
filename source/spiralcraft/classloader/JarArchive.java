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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import spiralcraft.vfs.file.FileResource;
import spiralcraft.vfs.util.ByteArrayResource;

/**
 * An archive contained in a Jar file
 * 
 * @author mike
 *
 */
public class JarArchive
  extends Archive
{
  private FileResource resource;
  private JarFile jarFile;
  
  public JarArchive(FileResource resource)
  { this.resource=resource;
  }
    
  @Override
  public void open()
    throws IOException
  {
    jarFile
      =new JarFile(resource.getFile(),false,JarFile.OPEN_READ);

  }
  
  @Override
  protected Entry loadEntry(String path)
    throws IOException
  {
    JarFile jarFile=get();
    
    try
    {
      JarEntry jarEntry=jarFile.getJarEntry(path);
      if (jarEntry!=null)
      { return new JarFileEntry(jarEntry);
      }
      else
      { return null;
      }
    }
    finally
    { 
      if (jarFile!=this.jarFile)
      { jarFile.close();
      }
    }
  }

  @Override
  public String toString()
  { return super.toString()+":"+resource.getURI();
  }
  
  @Override
  public void close()
  {
    if (jarFile!=null)
    {
      //ClassLog.getInstance(getClass()).fine("Closing "+resource.getURI());
      try
      { jarFile.close();
      }
      catch (IOException x)
      { }
      
      jarFile=null;
    }
    super.close();
  }  

  private JarFile get()
    throws IOException
  {
    JarFile jarFile=JarArchive.this.jarFile;
    if (jarFile==null)
    { 
      //ClassLog.getInstance(getClass()).fine("Reopening "+resource.getURI());
      jarFile
        =new JarFile(resource.getFile(),false,JarFile.OPEN_READ);
    }
    return jarFile;
  }
  
  public class JarFileEntry
    extends Entry
  {
    private final JarEntry jarEntry;
    
    public JarFileEntry(JarEntry jarEntry)
    { this.jarEntry=jarEntry;
    }

    @Override
    public String toString()
    { return super.toString()+": "+jarEntry.getName();
    }
    
    @Override
    public byte[] getData()
      throws IOException
    {
      JarFile jarFile=get();

      BufferedInputStream in=null;
      try
      {
        in = new BufferedInputStream(jarFile.getInputStream(jarEntry));

        byte[] data = new byte[(int) jarEntry.getSize()];
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
      finally
      {
        if (jarFile!=JarArchive.this.jarFile)
        { jarFile.close();
        }
      }
    }

    @Override
    public URL getResource()
      throws IOException
    { return new URL("jar:"+resource.getURI()+"!/"+jarEntry.getName());
    }

    @Override
    public InputStream getResourceAsStream()
      throws IOException
    { 
      JarFile jarFile=get();
      try
      { 
        InputStream in=jarFile.getInputStream(jarEntry);
        if (jarFile!=JarArchive.this.jarFile)
        {
          return ByteArrayResource
            .copyOf(jarFile.getInputStream(jarEntry))
            .getInputStream();
        }
        else
        { return in;
        }
      }
      finally
      { 
        if (jarFile!=JarArchive.this.jarFile)
        { jarFile.close();
        }
      }
    }
    
  }


}
