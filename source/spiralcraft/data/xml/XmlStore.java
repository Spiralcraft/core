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

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import spiralcraft.builder.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.spi.AbstractStore;


/**
 * Provides access to XML data 
 *
 * @author mike
 *
 */
public class XmlStore
  extends AbstractStore
{

  
  private LinkedHashMap<Type<?>,XmlQueryable> queryables
    =new LinkedHashMap<Type<?>,XmlQueryable>();
    
  private URI baseResourceURI;
  
  public void setBaseResourceURI(URI uri)
  { baseResourceURI=uri;
  }
  
  
  public XmlQueryable[] getQueryables()
  { 
    XmlQueryable[] list=new XmlQueryable[queryables.size()];
    queryables.values().toArray(list);
    return list;
    
  }
  
  public void setQueryables(XmlQueryable[] list)
  { 
    for (XmlQueryable queryable:list)
    { queryables.put(queryable.getResultType(),queryable);
    }
  }
  
 

  
  @SuppressWarnings("unchecked")
  @Override
  public DataConsumer getUpdater(
    Type type)
    throws DataException
  {
    // TODO Auto-generated method stub
    return null;
  }

  protected Queryable<Tuple> getQueryable(Type<?> type)
  { return queryables.get(type);
  }
  

  
  


  @Override
  public Type<?>[] getTypes()
  {
    Type<?>[] types=new Type[queryables.size()];
    int i=0;
    for (XmlQueryable queryable: queryables.values())
    { types[i++]=queryable.getResultType();
    }
    return types;
  }



  @Override
  public void start()
    throws LifecycleException
  {
    // TODO Auto-generated method stub
    for (XmlQueryable queryable:queryables.values())
    { 
      try
      { 
        queryable.setResourceContextURI(baseResourceURI);
        queryable.getAll(queryable.getResultType());
      }
      catch (DataException x)
      {
        // TODO Auto-generated catch block
        x.printStackTrace();
      }
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    // TODO Auto-generated method stub
    
  }

}
