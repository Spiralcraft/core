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
package spiralcraft.lang.optics;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Optic;

import java.util.HashMap;
import java.util.Map;


/**
 * A Prism which defines the contents of a Namespace. Multiple instances of
 *   a Namespace can be associated with one NamespacePrism.
 */
public class NamespacePrism
  implements Prism<Namespace>
{
  
  private int index=0;

  private Map<String,AttributeLense> attributeMap
    =new HashMap<String,AttributeLense>();
  
  
  
  @SuppressWarnings("unchecked") // Can't return Class of extended generic type
  public Class getContentType()
  { return Namespace.class;
  }
  
  
  @SuppressWarnings("unchecked") // Expression array params heterogeneous
  public synchronized <X> Binding<X> resolve
    (Binding<Namespace> source
    ,Focus<?> focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  { 
    AttributeLense lense=attributeMap.get(name);
    if (lense==null)
    { return null;
    }
    
    Binding<X> binding=source.getCache().<X>get(lense);
    if (binding==null)
    { 
      binding=new NamespaceBinding(source,lense);
      
      source.getCache().put(lense,binding);
    }
    return binding;
    
  }

  @SuppressWarnings("unchecked") // Dynamic class info
  public <D extends Decorator<Namespace>> D decorate
    (Binding<? extends Namespace> source,Class<D> decoratorInterface)
    throws BindException
  { return null;
  }
  
  public String toString()
  { return super.toString()+":"+getClass().getName();
  }

  @SuppressWarnings("unchecked") // We don't know specific type
  public void register(String name,Prism val)
    throws BindException
  { 
    if (attributeMap.get(name)!=null)
    { throw new BindException("NamespacePrism: Already registered '"+name+"'");
    }
    attributeMap.put(name,new AttributeLense(val,index++));
  }
  
  public int getAttributeCount()
  { return index;
  }
  
  Object getValue(Namespace namespace,String name)
    throws BindException
  { 
    AttributeLense lense=attributeMap.get(name);
    if (lense!=null)
    { return namespace.get(lense.getIndex());
    }
    else
    { throw new BindException("Name '"+name+"' not found");
    }
  }
  
  void setValue(Namespace namespace,String name,Object value)
    throws BindException
  { 
    AttributeLense lense=attributeMap.get(name);
    if (lense!=null)
    { namespace.set(lense.getIndex(),value);
    }
    else
    { throw new BindException("Name '"+name+"' not found");
    }
  }
  
  
}

class AttributeLense<T>
  implements Lense<T,Namespace>
{
  private final Prism<T> type;
  private final int index;
  
  public AttributeLense(Prism<T> type,int index)
  {
    this.type=type;
    this.index=index;
  }
  
  public Prism<T> getPrism()
  { return type;
  }

  @SuppressWarnings("unchecked") // Heterogeneous collection
  public T translateForGet(Namespace source, Optic[] modifiers)
  { return (T) source.get(index);
  }

  
  public Namespace translateForSet(T source, Optic[] modifiers)
  { throw new UnsupportedOperationException("Operation is not reversible");
  }
  
  int getIndex()
  { return index;
  }
  
}


