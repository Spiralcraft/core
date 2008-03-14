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
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;


import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.util.InstanceResolver;

import java.net.URI;
import java.util.Collection;

/**
 * Base type for Collections
 */
@SuppressWarnings("unchecked") // Runtime resolution
public class AbstractCollectionType<T extends Collection>
  extends AbstractAggregateType<T>
{  
  private final TypeResolver resolver;
  
  @SuppressWarnings("unchecked")
  public AbstractCollectionType
    (TypeResolver resolver
    ,Type<? super Object> contentType
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
        this.contentType=(Type<? super Object>) getTypeResolver().resolve
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

  
  
  
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  

  public ValidationResult validate(Object value)
  { 
    // More work here-
    //   Go through values and and call type.validate() on each one
    // Validation result should accept multiple messages
    return null;
  }
  
  
  @SuppressWarnings("unchecked")
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
      { collection=(T) nativeClass.newInstance();
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
        Type valueType=((DataComposite) val).getType();
        Object convertedVal=valueType.fromData((DataComposite) val,resolver);
        collection.add(convertedVal);
      }
    }
    return collection;
  }
  
  public DataComposite toData(T obj)
    throws DataException
  { 
    if (!(obj instanceof Collection))
    { throw new IllegalArgumentException("Not a collection");
    }
    
    Collection<?> collection=(Collection<?>) obj;
    
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
      EditableAggregate<DataComposite> aggregate
      	=new EditableArrayListAggregate<DataComposite>(this);
      for (Object o: collection)
      { aggregate.add(contentType.toData(o));
      }
      return aggregate;
    }
  }
  
  protected String getAggregateQualifier()
  { return ".list";
  }





}
