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
package spiralcraft.log.jul;

import java.util.logging.Logger;

import spiralcraft.registry.RegistryPathObject;
import spiralcraft.registry.RegistryNode;

/**
 * A logger which derives its name from the Registry path
 */
public class RegistryLogger
  extends Logger
  implements RegistryPathObject
{

  /**
   * Create root RegistryLogger
   */
  public RegistryLogger()
  { 
    super("",null);
  }

  public RegistryLogger(RegistryLogger parent,String name)
  { 
    super(name,null);
    setParent(parent);
  }
  
  public synchronized RegistryPathObject registryPathObject(RegistryNode registryNode)
  { 
    if (getName().equals(""))
    { return new RegistryLogger(this,registryNode.getName());
    }
    else
    { return new RegistryLogger(this,getName()+"."+registryNode.getName());
    }
  }

}
