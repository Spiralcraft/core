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
package spiralcraft.ui;

import spiralcraft.common.LifecycleException;
import spiralcraft.service.ServiceAdapter;


/**
 * Generic UI service, typically subclassed for a soecific View, 
 *   such as Swing or WebUI. 
 */
public class UiService
  extends ServiceAdapter
{
  private Control _rootControl;

  public void setRootControl(Control val)
  { _rootControl=val;
  }

  @Override
  public void start()
    throws LifecycleException
  { _rootControl.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { _rootControl.stop();
  }

}
