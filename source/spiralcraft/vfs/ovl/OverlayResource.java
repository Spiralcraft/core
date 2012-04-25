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
package spiralcraft.vfs.ovl;

//
//import spiralcraft.log.ClassLog;
//import spiralcraft.log.Level;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.UnresolvableURIException;


/**
 * <p>A virtual resource that overlays one path tree over another. Resources
 *   that do not exist in the "overlay" tree will be searched for in the 
 *   "base" tree. New resources will be created in the "overlay" tree.
 * </p>
 * 
 * <p>An OverlayResource uses the internal "ovl" URI scheme, which provides
 *   access to a set of contextual roots by mapping the authority component of
 *   the URI.
 * </p>
 * 
 * <p>An OverlayResource is a wrapper for the resolved Resources.
 * </p>
 * 
 */
public class OverlayResource
  implements Resource,Container
{ 

  private static final ClassLog log=
      ClassLog.getInstance(OverlayResource.class);
    
  
  
  public static final Resource wrap(Resource overlay)
  {
    if (overlay instanceof OverlayResource)
    { return overlay;
    }
    
    Container container=overlay.asContainer();
    if (container!=null)
    {
      Resource child;
      try
      { child=container.getChild(".sc-ovl");
      }
      catch (UnresolvableURIException x)
      { throw new RuntimeException(x);
      }
      
      try
      {  
        Resource base;
        if (child.exists())
        {
          Properties props=new Properties();
          props.load(child.getInputStream());
          String baseURIStr=props.getProperty("base");
          if (baseURIStr!=null)
          {
            URI baseURI=URI.create(baseURIStr);
            if (!baseURI.isAbsolute())
            { baseURI=child.getURI().resolve(baseURI);
            }
            log.fine("Base is "+baseURI);
            base=wrap(Resolver.getInstance().resolve(baseURI));
          
            return new OverlayResource
              (overlay.getURI()
              ,overlay
              ,base
              );
          }
          return overlay;
        }
        else
        { return overlay;
        }
        
      }
      catch (IOException x)
      { 
        throw new IllegalArgumentException
          ("Error reading overlay resource "+child.getURI().toString(),x);
      }
    }
    else
    { return overlay;
    }
  }

  
  private final Resource overlay;
  private final Resource base;
  private final URI uri;
  
  public OverlayResource
    (URI uri
    ,Resource overlay
    ,Resource base
    )
  { 
    this.uri=uri;
    this.overlay=overlay;
    this.base=base;
  }
  
  @Override
  public URI getURI()
  { return uri;
  }
  
  @Override
  public URI getResolvedURI()
  { return uri;
  }
  
  @Override
  public void delete()
    throws IOException
  { 
    if (overlay.exists())
    { overlay.delete();
    }
    
    if (base.exists())
    { base.delete();
    }
  }
  
  @Override
  public void renameTo(URI uri)
    throws IOException
  {
    if (overlay.exists())
    { overlay.renameTo(uri);
    }
    else if (base.exists())
    { base.renameTo(uri);
    }
  }
  
  @Override
  public InputStream getInputStream()
    throws IOException
  {
    if (overlay.exists())
    { return overlay.getInputStream();
    }
    else
    { return base.getInputStream();
    }
  }

  @Override
  public OutputStream getOutputStream()
    throws IOException
  { return overlay.getOutputStream();
  }

  @Override
  public boolean exists()
    throws IOException
  { return overlay.exists() || base.exists();
  }
  
  @Override
  public long getLastModified() 
    throws IOException
  { 
    if (overlay.exists())
    { return overlay.getLastModified();
    }
    else
    { return base.getLastModified();
    }
  }
  
  @Override
  public boolean setLastModified(long lastModified)
    throws IOException
  {
    if (overlay.exists())
    { return overlay.setLastModified(lastModified);
    }
    else return false;
  }
  
  @Override
  public long getSize() 
    throws IOException
  {
    if (overlay.exists())
    { return overlay.getSize();
    }
    else
    { return base.getSize();
    }
  }
  
  
  @Override
  public Container asContainer()
  { 
    if (overlay.asContainer()!=null || base.asContainer()!=null)
    { return this;
    }
    else
    { return null;
    }
  }
  
  @Override
  public Resource getChild(String name)
  { 
    try
    {
      return new OverlayResource
        (URIUtil.addUnencodedPathSegment(uri,name)
        ,overlay.asContainer().getChild(name)
        ,base.asContainer().getChild(name)
        );
    }
    catch (UnresolvableURIException x)
    { throw new IllegalArgumentException("Invalid name "+name,x);
    }
  }
  
  @Override
  public String toString()
  { 
    return super.toString()
      +"["+overlay.toString()+"] -> ["+base.toString()+"]";
  }

  @Override
  public Resource[] listContents()
    throws IOException
  {
    return ArrayUtil.concat
      (Resource[].class
      ,overlay.asContainer().listContents()
      ,base.asContainer().listContents()
      );
  }

  @Override
  public Resource[] listChildren()
    throws IOException
  {
    return ArrayUtil.concat
      (Resource[].class
      ,overlay.asContainer().listChildren()
      ,base.asContainer().listChildren()
      );
  }

  @Override
  public Resource[] listChildren(
    ResourceFilter filter)
    throws IOException
  {
    return ArrayUtil.concat
      (Resource[].class
      ,overlay.asContainer().listChildren(filter)
      ,base.asContainer().listChildren(filter)
      );
  }

  @Override
  public Resource[] listLinks()
    throws IOException
  {
    return ArrayUtil.concat
      (Resource[].class
      ,overlay.asContainer().listLinks()
      ,base.asContainer().listLinks()
      );
  }

  @Override
  public Container ensureChildContainer(
    String name)
    throws IOException
  {
    return new OverlayResource
      (URIUtil.addUnencodedPathSegment(uri,name)
      ,overlay.asContainer().ensureChildContainer(name)
      ,base.asContainer().getChild(name)
      );
  }

  @Override
  public Resource createLink(
    String name,
    Resource resource)
    throws UnresolvableURIException
  { 
    return overlay.asContainer().createLink(name,resource);
  }


  @Override
  public boolean supportsRead()
  { return overlay.supportsRead() || base.supportsRead();
  }

  @Override
  public boolean supportsWrite()
  { return overlay.supportsWrite() || base.supportsWrite();
  }

  @Override
  public Resource[] getChildren()
    throws IOException
  { 
    return ArrayUtil.concat
      (Resource[].class
      ,overlay.asContainer().getChildren()
      ,base.asContainer().getChildren()
      );
  }

  @Override
  public Container ensureContainer()
    throws IOException
  { return overlay.ensureContainer(); 
  }

  public Resource getOverlay()
  { return overlay;
  }
  
  public Resource getBase()
  { return base;
  }
  
  @Override
  public Resource getParent()
    throws IOException
  {
    if (uri.getPath()==null 
        || uri.getPath().isEmpty() 
        || uri.getPath().equals("/")
       )
    { return null;
    }
    
    return new OverlayResource
      (URIUtil.toParentPath(uri)
      ,overlay.getParent()
      ,base.getParent()
      );
  }

  @Override
  public String getLocalName()
  { return URIUtil.unencodedLocalName(uri);
  }

  @Override
  public void copyFrom(
    Resource source)
    throws IOException
  { overlay.copyFrom(source);
  }

  @Override
  public void copyTo(
    Resource targetResource)
    throws IOException
  {
    if (overlay.exists())
    { overlay.copyTo(targetResource);
    }
    else
    { base.copyTo(targetResource);
    }
  }

  @Override
  public void moveTo(Resource targetResource)
    throws IOException
  {
    if (overlay.exists())
    { overlay.moveTo(targetResource);
    }
    else
    { base.moveTo(targetResource);
    }
    // TODO Auto-generated method stub
    
  }

  @Override
  public long write(
    InputStream in)
    throws IOException
  { return overlay.write(in);
  }

  @Override
  public <T extends Resource> T unwrap(
    Class<T> clazz)
  {
    T ret=overlay.unwrap(clazz);
    if (ret==null)
    { ret=base.unwrap(clazz);
    }
    return ret;
  }
  
}
