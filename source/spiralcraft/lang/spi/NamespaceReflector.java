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
package spiralcraft.lang.spi;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>A Reflector which defines the contents of a Namespace. Multiple instances of
 *   a Namespace can be associated with one NamespaceReflector.
 * </p>
 * 
 * <h3>EXPERIMENTAL CODE</h3>
 * <p>This class is experimental. It's necessity lies in
 *   exposing as strongly typed named Channels members of a collection that are
 *   heterogeneously typed. Specifically, the set of named Views in a dataset,
 *   which expose different data types.
 * </p>
 *   
 * <p>This may be also accomplished by spiralcraft.data
 *   methods, or by using chained Focus objects that expose various interfaces.
 *   Because use of this requires an object to generate a focus, it is possible
 *   that simply using chained Focus objects would achieve the same result with
 *   greater elegance and simplicity.
 * </p>
 *   
 * 
 */
public class NamespaceReflector
  implements Reflector<Namespace>
{
  
  private int index=0;

  private Map<String,NamespaceAttribute<?>> attributeMap
    =new HashMap<String,NamespaceAttribute<?>>();
  
  
  
  public Class<Namespace> getContentType()
  { return Namespace.class;
  }
  
  
  @SuppressWarnings("unchecked") // Expression array params heterogeneous
  public synchronized <X> Channel<X> resolve
    (Channel<Namespace> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  { 
    NamespaceAttribute translator=attributeMap.get(name);
    
    if (translator==null)
    { return null;
    }
    
    Channel<X> binding=source.<X>getCached(translator);
    if (binding==null)
    { 
      binding=new NamespaceChannel(source,translator);
      
      source.cache(translator,binding);
    }
    return binding;
    
  }

  public <D extends Decorator<Namespace>> D decorate
    (Channel<Namespace> source,Class<D> decoratorInterface)
    throws BindException
  { return null;
  }
  

  @SuppressWarnings("unchecked") // We don't know specific type
  public void register(String name,Reflector val)
    throws BindException
  { 
    if (attributeMap.get(name)!=null)
    { throw new BindException("NamespaceReflector: Already registered '"+name+"'");
    }
    attributeMap.put(name,new NamespaceAttribute(val,index++));
  }
  
  public int getAttributeCount()
  { return index;
  }
  
  Object getOptic(Namespace namespace,String name)
    throws BindException
  { 
    NamespaceAttribute<?> translator=attributeMap.get(name);
    if (translator!=null)
    { return namespace.getOptic(translator.getIndex());
    }
    else
    { throw new BindException("Name '"+name+"' not found");
    }
  }
  
  void putOptic(Namespace namespace,String name,Channel<?> value)
    throws BindException
  { 
    NamespaceAttribute<?> translator=attributeMap.get(name);
    if (translator!=null)
    { namespace.setOptic(translator.getIndex(),value);      
    }
    else
    { throw new BindException("Name '"+name+"' not found");
    }
  }
  
  public String toString()
  { return super.toString()+attributeMap.keySet();
  }

  @Override
  public URI getTypeURI()
  {
    // TODO Auto-generated method stub
    // XXX: Not defined yet
    return null;
  }


  @Override
  public boolean isAssignableTo(
    URI typeURI)
  {
    // TODO Auto-generated method stub
    // XXX: Not defined yet
    return false;
  }
  
}



