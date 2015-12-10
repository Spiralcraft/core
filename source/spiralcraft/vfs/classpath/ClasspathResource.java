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

import spiralcraft.log.ClassLog;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.spi.AbstractResource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class ClasspathResource
  extends AbstractResource
  implements Container
{
  private static final ClassLog log
    =ClassLog.getInstance(ClasspathResource.class);

  public static final URI stripTrailingSlash(URI uri)
  { 
    if (uri.getPath()==null)
    { 
      throw new IllegalArgumentException
        ("URI "+uri+" has a null path component");
    }
    
    if (uri.getPath().endsWith("/"))
    { 
      try
      {
        return URIPool.get
          (new URI
            (uri.getScheme()
            ,null
            ,uri.getPath().length()>1
              ?uri.getPath().substring(0,uri.getPath().length()-1)
              :"/"
            ,null
            ,null
            )
          );
      }
      catch (URISyntaxException x)
      { throw new IllegalArgumentException("Failed to parse URI "+uri,x);
      }
    }
    else
    { return uri;
    }
  }
  
  private final WeakReference<ClassLoader> _classLoader;
  private final String _path;
  private URL _url;
  private List<String> _contents;


  
  public ClasspathResource(URI uri)
  { 
    super(stripTrailingSlash(uri),uri);
    if (getURI().getPath().length()>0)
    {
      _path=getURI().getPath().substring(1);
      _classLoader
        =new WeakReference<ClassLoader>
          (Thread.currentThread().getContextClassLoader());
    }
    else
    { throw new IllegalArgumentException("Invalid URI "+uri);
    }
  }

  
  @Override
  public InputStream getInputStream()
    throws IOException
  { return _classLoader.get().getResourceAsStream(_path);
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
    { _url=_classLoader.get().getResource(_path);
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
  
  @Override
  public Container asContainer()
  { 
    boolean debug=false;
    try
    {
      if (_contents==null)
      { 
        if (debug)
        { log.fine("Checking "+_path+" in "+_classLoader);
        }
        Enumeration<URL> resources=_classLoader.get().getResources(_path);
        
        List<URL> parts=new LinkedList<URL>();
        List<String> contents=new LinkedList<String>();
        while (resources.hasMoreElements())
        { 
          
          parts.add(resources.nextElement());
          
          if (debug)
          { log.fine("Got "+parts.get(parts.size()-1).toString());
          }
        }
        if (parts.size()>0)
        { 
          for (URL url: parts)
          { 
            try
            { 
              Resource partResource
                =Resolver.getInstance().resolve(url.toURI());
              if (debug)
              { log.fine("Checking part "+partResource);
              }
              
              Container partContainer=partResource.asContainer();
              if (partContainer!=null)
              {
                if (debug)
                { log.fine("Listing container part "+partContainer);
                }
                
                for (Resource resource:partContainer.listChildren())
                { 
                  
                  if (debug)
                  { log.fine("Adding child  "+resource);
                  }
                  contents.add(resource.getLocalName());
                }
              }
            }
            catch (URISyntaxException x)
            { x.printStackTrace();
            }
          }
          if (contents.size()>0)
          { this._contents=contents;
          }
        }
      }
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    
    return _contents!=null?this:null;
  }

  
  @Override
  public void renameTo(URI name)
  { 
    throw new UnsupportedOperationException
      ("A classpath resource cannot be renamed");
  }  

  @Override
  public boolean exists()
    throws IOException
  { return _classLoader.get().getResource(_path) !=null;
  }
  
  @Override
  public void delete()
    throws IOException
  { throw new IOException("ClasspathResource is read-only");
  }

  
  @Override
  public ClasspathResource getChild(String name)
  { 
    if (_path.length()>0)
    {
      URI child=URIPool.create(getURI().getScheme()+":/"+_path+"/"+name);
      return new ClasspathResource
        (child);
    }
    else
    {
      URI child=URIPool.create(getURI().getScheme()+":/"+name);
      return new ClasspathResource
        (child);
    }
  }


  @Override
  public Resource createLink(
    String name,
    Resource resource)
    throws UnresolvableURIException
  { return null;
  }

  @Override
  public Resource[] listChildren()
    throws IOException
  { 
    Resource[] children=new Resource[_contents.size()];
    int i=0;
    for (String childName:_contents)
    { children[i++]=getChild(childName);
    }
    return children;
  }

  @Override
  public Resource[] listContents()
    throws IOException
  { return listChildren();
  }

  @Override
  public Resource[] listLinks()
    throws IOException
  { return null;
  }
  
  @Override
  public URL getURL()
  { return _classLoader.get().getResource(_path);
  }
}
