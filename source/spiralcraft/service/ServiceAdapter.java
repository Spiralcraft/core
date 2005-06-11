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

public class ServiceAdapter
  implements Service
{

  private Object _selector;

  public void init(ServiceResolver resolver)
    throws ServiceException
  {
  }

  public void destroy()
    throws ServiceException
  {
  }

  public boolean providesInterface(Class serviceClass)
  { return serviceClass.isAssignableFrom(getClass());
  }

  public Object getInterface(Class serviceClass)
  { 
    if (serviceClass.isAssignableFrom(getClass()))
    { return this;
    }
    else
    { return null;
    }
  }

  public Object getSelector()
  { return _selector;
  }

  public void setSelector(Object val)
  { _selector=val;
  }

}
