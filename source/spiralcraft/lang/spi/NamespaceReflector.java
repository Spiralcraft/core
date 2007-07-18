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

import java.util.HashMap;
import java.util.Map;


/**
 * A Reflector which defines the contents of a Namespace. Multiple instances of
 *   a Namespace can be associated with one NamespaceReflector.
 */
public class NamespaceReflector
  implements Reflector<Namespace>
{
  
  private int index=0;

  private Map<String,NamespaceAttribute<?>> attributeMap
    =new HashMap<String,NamespaceAttribute<?>>();
  
  
  
  @SuppressWarnings("unchecked") // Can't return Class of extended generic type
  public Class getContentType()
  { return Namespace.class;
  }
  
  
  @SuppressWarnings("unchecked") // Expression array params heterogeneous
  public synchronized <X> Binding<X> resolve
    (Binding<Namespace> source
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
    
    Binding<X> binding=source.getCache().<X>get(translator);
    if (binding==null)
    { 
      binding=new NamespaceBinding(source,translator);
      
      source.getCache().put(translator,binding);
    }
    return binding;
    
  }

  @SuppressWarnings("unchecked") // Dynamic class info
  public <D extends Decorator<Namespace>> D decorate
    (Binding<? extends Namespace> source,Class<D> decoratorInterface)
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
  
}



