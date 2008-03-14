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


import spiralcraft.data.spi.AbstractAggregateQueryable;

import spiralcraft.data.sax.DataReader;

import spiralcraft.data.Aggregate;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;


import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.util.Watcher;
import spiralcraft.vfs.util.WatcherHandler;


import java.net.URI;
import java.io.IOException;

import spiralcraft.log.ClassLogger;

/**
 * <P>Provides basic query functionality for an XML document which contains 
 *   an Aggregate (a set of Tuples of a common Type)
 * 
 * <P>The document is polled for updates
 * 
 * @author mike
 */
public class XmlQueryable
  extends AbstractAggregateQueryable<Tuple>
{
  @SuppressWarnings("unused")
  private static final ClassLogger log=ClassLogger.getInstance(XmlQueryable.class);
  
  private Resource resource;
  private URI resourceURI;
  private URI resourceContextURI;
  
  private Watcher watcher;
  
  private Type<?> type;
  
  private Aggregate<Tuple> aggregate;
  
  private WatcherHandler handler
    =new WatcherHandler()
    {
      @SuppressWarnings("unchecked") // Downcast
      public int handleUpdate()
      {
        DataReader reader=new DataReader();
        try
        {
          // log.fine("Resource "+resource.getURI()+" changed");
          Aggregate<Tuple> data=(Aggregate<Tuple>) reader.readFromResource
            (resource, type);
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
  
  public XmlQueryable(Type<?> type,URI resourceURI)
  {
    this.type=type;
    setResourceURI(resourceURI);
  }
  
  protected void checkInit()
    throws DataException
  {
    if (type==null)
    { throw new DataException("No Type configured");
    }

    
    if (resource==null)
    { 
      if (resourceURI==null)
      { throw new DataException("No resourceURI specified");
      }
      
      try
      { 
        URI qualifiedURI
          =resourceURI.isAbsolute()
          ?resourceURI
          :resourceContextURI!=null
            ?resourceContextURI.resolve(resourceURI)
            :resourceURI
          ;
            
        Resource resource=Resolver.getInstance().resolve(qualifiedURI);
        try
        {
          if (!resource.exists())
          { throw new DataException(qualifiedURI+" does not exist");
          }
        }
        catch (IOException x)
        { throw new DataException(qualifiedURI+" could not be read",x);
        }
        setResource(resource);
      }
      catch (UnresolvableURIException x)
      { 
        throw new DataException
          ("Error resolving "+resourceURI
          +(resourceContextURI!=null
           ?" in context "+resourceContextURI
           :""
           )
          ,x
          );
      }
      
      
    }
          
    if (watcher==null)
    { watcher=new Watcher(resource,1000,handler);    
    }
  }
  
  @Override
  protected Aggregate<Tuple> getAggregate()
    throws DataException
  { 
    checkInit();

    // log.fine("Checking resource");
    watcher.check();
    return aggregate;
  }
  
  @Override
  // This is the Type of the Queryable, not the data container
  protected Type<?> getResultType()
  { return type.getContentType();
  }
  
  public void setResultType(Type<?> type)
  { this.type=Type.getAggregateType(type);
  }
  
  private void setAggregate(Aggregate<Tuple> aggregate)
  { this.aggregate=aggregate;
  }
  
  /**
   * 
   * @param typeURI The TypeURI contained in the XML resource. This is usually
   *   a list type of the resultType.
   */
  public void setTypeURI(URI typeURI)
  { 
    try
    { this.type=Type.resolve(typeURI);
    }
    catch (DataException x)
    { throw new IllegalArgumentException(x);
    }
  }
  
  public void setResourceURI(URI resourceURI)
  { 
    // log.fine("XmlQueryable: uri="+resourceURI);
    this.resourceURI=resourceURI;
  }

  public void setResourceContextURI(URI resourceContextURI)
  { 
    // log.fine("XmlQueryable: uri="+resourceURI);
    this.resourceContextURI=resourceContextURI;
  }
  
  
  
  public void setResource(Resource resource)
  { 
    this.resource=resource;
  }
  
  
  
}
