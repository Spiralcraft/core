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
package spiralcraft.registry;

import java.util.HashMap;

/**
 * A RegistryNode which resides in the local VM and
 *   is accessed via an object reference.
 */
public class LocalRegistryNode
  implements RegistryNode
{
  // XXX Make maps weak
  private HashMap<Class<?>,Object> _instances;
  private HashMap<String,RegistryNode> _children;
  private final RegistryNode _parent;
  private final String _name;
  private final String _absolutePath;

  public LocalRegistryNode(RegistryNode parent,String name)
  { 
    _parent=parent;
    _name=name;
    if (_parent==null)
    { _absolutePath="/";
    }
    else if (_parent.getAbsolutePath().equals("/"))
    { _absolutePath="/"+_name;
    }
    else
    { _absolutePath=_parent.getAbsolutePath()+"/"+_name;
    }
  }

  public String getName()
  { return _name;
  }

  public String getAbsolutePath()
  { return _absolutePath;
  }

  @SuppressWarnings("unchecked")
  public <X> X findInstance(Class<X> instanceClass)
  { 
    X instance=null;
    if (_instances!=null)
    { instance=(X) _instances.get(instanceClass);
    }
    
    if (instance==null && _parent!=null)
    { 
      instance=_parent.findInstance(instanceClass);
      if (instance!=null && instance instanceof RegistryPathObject)
      { 
        instance=(X) ((RegistryPathObject) instance).registryPathObject(this);
        registerInstance(instanceClass,instance);
      }
    }
    return instance;
  }

  public RegistryNode getChild(String name)
  { 
    if (_children!=null)
    { return _children.get(name);
    }
    return null;
  }

  public RegistryNode createChild
    (Class<?> instanceClass,Object instance)
  {
    RegistryNode child=createChild(instanceClass.getName());
    child.registerInstance(instanceClass,instance);
    return child;
  }
  
  public void registerInstance(Class<?> instanceClass,Object instance)
  { 
    if (_instances==null)
    { _instances=new HashMap<Class<?>,Object>();
    }
    _instances.put(instanceClass,instance);
  }

  public RegistryNode createChild(String name)
  { 
    if (_children==null)
    { _children=new HashMap<String,RegistryNode>();
    }
    
    String newName=name;
    int i=1;
    while (_children.get(newName)!=null)
    { newName=name+i++;
    }

    RegistryNode child=new LocalRegistryNode(this,newName);
    _children.put(newName,child);
    return child;
  }

  @Override
  public String toString()
  { 
    StringBuffer out=new StringBuffer();
    out.append(super.toString());
    out.append(": path=").append(_absolutePath);
    if (_instances!=null)
    { 
      out.append(" :Instances=").append(_instances.keySet().toString());
      Object primary=_instances.get(Object.class);
      if (primary!=null)
      { out.append(" :Object=").append(primary.getClass().getName());
      }
    }
    return out.toString();
  }

  
}
