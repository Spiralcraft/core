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
package spiralcraft.service;

import spiralcraft.common.LifecycleException;
import spiralcraft.registry.RegistryNode;

/**
 * A group of Services managed as unit
 */
public class ServiceGroup
  implements Service
{
  private Service[] _services;

  public void setServices(Service[] services)
  { _services=services;
  }

  public Service[] getServices()
  { return _services;
  }

  
  @Override
  public void register(RegistryNode node)
  {
    for (Service service : _services)
    { service.register(node);
    }
  }
  
  /**
   * Initialize Services. If an exception occurs, all Services
   *   previously initialized will be destroyed before an
   *   exception is thrown from this method.
   */
  public void start()
    throws LifecycleException
  { 

    if (_services==null)
    { return;
    }

    for (int i=0;i<_services.length;i++)
    { 
      try
      { _services[i].start();
      }
      catch (LifecycleException x)
      { 
        for (int j=i-1;j>=0;j--)
        { 
          try
          { _services[j].stop();
          }
          catch (LifecycleException y)
          { y.printStackTrace();
          }
        }
        throw x;
      }
    }
  }

  /**
   * Destroy Services. If an exception occurs, it will be
   *   logged and remaining Services will be destroyed before
   *   an exception is thrown. If multiple exceptions occur,
   *   the last exception will be thrown and the rest will be
   *   logged.
   */
  public void stop()
    throws LifecycleException
  {
    if (_services==null)
    { return;
    }

    LifecycleException exception=null;
    for (int i=_services.length;i-->0;)
    { 
      try
      { _services[i].stop();
      }
      catch (LifecycleException x)
      { 
        if (exception!=null)
        { exception.printStackTrace();
        }
        exception=x;
      }
    }
    if (exception!=null)
    { throw exception;
    }
  }


}
