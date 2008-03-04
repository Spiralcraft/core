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

import spiralcraft.builder.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.spi.AbstractStore;
import spiralcraft.data.spi.BaseExtentQueryable;


/**
 * Provides access to XML data 
 *
 * @author mike
 *
 */
public class XmlStore
  extends AbstractStore
{

  
  private LinkedHashMap<Type<?>,Queryable<Tuple>> queryables
    =new LinkedHashMap<Type<?>,Queryable<Tuple>>();
    
  private ArrayList<XmlQueryable> xmlQueryables
    =new ArrayList<XmlQueryable>();
  
  private URI baseResourceURI;
  
  public void setBaseResourceURI(URI uri)
  { baseResourceURI=uri;
  }
  
  
//  public XmlQueryable[] getQueryables()
//  { 
//    XmlQueryable[] list=new XmlQueryable[queryables.size()];
//    queryables.values().toArray(list);
//    return list;
//    
//  }
  
  @SuppressWarnings("unchecked")
  public void setQueryables(XmlQueryable[] list)
  { 
    
    for (XmlQueryable queryable:list)
    { 
      xmlQueryables.add(queryable);
      
      Type<?> subtype=queryable.getResultType();
      queryables.put(subtype,queryable);
      
      Type<?> type=subtype.getBaseType();
      while (type!=null)
      { 
        // Set up a queryable for each of the XmlQueryable's base types
        
        Queryable<?> candidateQueryable=queryables.get(type);
        BaseExtentQueryable baseQueryable;
        
        if (candidateQueryable==null)
        { 
          baseQueryable=new BaseExtentQueryable(type);
          queryables.put(type, baseQueryable);
          baseQueryable.addExtent(subtype,queryable);
        }
        else if (!(candidateQueryable instanceof BaseExtentQueryable))
        {
          // The base extent queryable is already "concrete"
          // This is ambiguous, though. The base extent queryable only
          //   contains the non-subtyped concrete instances of the
          //   base type.
          
          baseQueryable=new BaseExtentQueryable(type);
          queryables.put(type, baseQueryable);
          baseQueryable.addExtent(type,candidateQueryable);
          baseQueryable.addExtent(subtype,queryable);
        }
        else
        {
          ((BaseExtentQueryable) candidateQueryable)
            .addExtent(subtype, queryable);
        }
        type=type.getBaseType();
        
      }
      
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
    for (Queryable<Tuple> queryable: queryables.values())
    { types[i++]=queryable.getTypes()[0];
    }
    return types;
  }



  @Override
  public void start()
    throws LifecycleException
  {
    // TODO Auto-generated method stub
    for (XmlQueryable queryable:xmlQueryables)
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
