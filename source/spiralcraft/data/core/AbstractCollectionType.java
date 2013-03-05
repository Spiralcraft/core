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
package spiralcraft.data.core;

import spiralcraft.data.Type;
import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.util.InstanceResolver;

import java.net.URI;
import java.util.Collection;

/**
 * Base type for Collections
 */
@SuppressWarnings({ "unchecked", "rawtypes" }) // Runtime resolution
public class AbstractCollectionType<T extends Collection,Tcontent>
  extends AbstractAggregateType<T,Tcontent>
{  
  private final TypeResolver resolver;
  
  public AbstractCollectionType
    (TypeResolver resolver
    ,Type<Tcontent> contentType
    ,URI uri
    ,Class<T> nativeClass
    )
  { 
    super(uri);
    this.resolver=resolver;
    if (contentType==null)
    { 
      try
      {
        this.contentType=getTypeResolver().resolve
          (ReflectionType.canonicalURI(Object.class));
      }
      catch (DataException x)
      { throw new RuntimeException(x);
      }
    }
    else
    { this.contentType=contentType;
    }
    this.nativeClass=nativeClass;
  }

  
  
  
  @Override
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  
  
  @Override
  public T fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { 
    Aggregate aggregate=data.asAggregate();
    
    T collection=null;
    
    if (resolver!=null)
    { collection=(T) resolver.resolve(nativeClass);
    }
    
    if (collection==null)
    { 
      try
      { collection=nativeClass.newInstance();
      }
      catch (InstantiationException x)
      { 
        throw new DataException
          ("Error instantiating collection "
          +nativeClass.getName()+": "+x.toString()
          ,x
          );
      }
      catch (IllegalAccessException x)
      { 
        throw new DataException
          ("Error instantiating collection "
          +nativeClass.getName()+": "+x.toString()
          ,x
          );
      }
      
    }
    
    for (Object val: aggregate)
    { 
//      if (contentType.isPrimitive())
      if (!(val instanceof DataComposite))
      { collection.add(val);
      }
      else
      { 
        Type<Tcontent> valueType
          =(Type<Tcontent>) ((DataComposite) val).getType();
        Tcontent convertedVal=valueType.fromData((DataComposite) val,resolver);
        collection.add(convertedVal);
      }
    }
    return collection;
  }
  
  @Override
  public DataComposite toData(T collection)
    throws DataException
  { 
//    if (!(obj instanceof Collection))
//    { throw new IllegalArgumentException("Not a collection");
//    }
//    
//    Collection<?> collection=(Collection<?>) obj;
    
    if (contentType.isPrimitive())
    {
      EditableAggregate<Object> aggregate
      	=new EditableArrayListAggregate<Object>(this);
    
      for (Object o: collection)
      { aggregate.add(o);
      }
      return aggregate;
    }
    else
    {
      EditableAggregate<Object> aggregate
      	=new EditableArrayListAggregate<Object>(this);
      for (Tcontent o: (Collection<Tcontent>) collection)
      { 
        Object item;
        
        if (o==null || o.getClass().equals(contentType.getNativeClass()))
        { item=contentType.toData(o);
        }
        else
        { 
          Type itemType=ReflectionType.<Tcontent>canonicalType
            ((Class<Tcontent>) o.getClass());
          if (itemType.isDataEncodable())
          { item=itemType.toData(o);
          }
          else
          { item=o;
          }
        }
        aggregate.add(item);
      }
      return aggregate;
    }
  }
  
  @Override
  protected String getAggregateQualifier()
  { return ".list";
  }





}
