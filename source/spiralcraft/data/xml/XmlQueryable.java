//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.xml;


import spiralcraft.data.spi.AggregateQueryable;

import spiralcraft.data.sax.DataReader;

import spiralcraft.data.Aggregate;
import spiralcraft.data.Tuple;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataException;


import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.util.Watcher;
import spiralcraft.vfs.util.WatcherHandler;


import java.net.URI;
import java.io.IOException;

/**
 * <P>Provides basic query functionality for an XML document which contains 
 *   an Aggregate (a set of Tuples of a common Type)
 * 
 * <P>The document is polled for updates
 * 
 * @author mike
 */
public class XmlQueryable
  extends AggregateQueryable<Tuple>
{
  private Resource resource;
  
  private Watcher watcher;
  
  private URI typeURI;
  private WatcherHandler handler
    =new WatcherHandler()
    {
      @SuppressWarnings("unchecked") // Downcast
      public int handleUpdate()
      {
        DataReader reader=new DataReader();
        try
        {
          Aggregate<Tuple> data=(Aggregate<Tuple>) reader.readFromResource
            (resource, TypeResolver.getTypeResolver().resolve(typeURI));
          setAggregate(data);
        }
        catch (Exception x)
        { 
          x.printStackTrace();
          return -1000;
        }
        return 0;
      }
    };
  
  public XmlQueryable() 
    throws DataException
  { super();
  }
  
  public Aggregate<Tuple> getAggregate()
  { 
    watcher.check();
    return super.getAggregate();
  }
  
  public void setTypeURI(URI typeURI)
  { this.typeURI=typeURI;
  }
  
  public void setResourceURI(URI resourceURI)
  { 
    System.err.println("XmlQueryable: uri="+resourceURI);
    try
    { 
      Resource resource=Resolver.getInstance().resolve(resourceURI);
      try
      {
        if (!resource.exists())
        { throw new IllegalArgumentException(resourceURI+" does not exist");
        }
      }
      catch (IOException x)
      { throw new IllegalArgumentException(resourceURI+" could not be read");
      }
      setResource(Resolver.getInstance().resolve(resourceURI));
    }
    catch (UnresolvableURIException x)
    { throw new IllegalArgumentException(x.toString(),x);
    }
  }
  
  public void setResource(Resource resource)
  { 
    this.resource=resource;
    watcher=new Watcher(resource,1000,handler);    
  }
  
  
  
}
