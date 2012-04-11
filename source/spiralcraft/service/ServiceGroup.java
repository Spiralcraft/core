//
// Copyright (c) 2011 Michael Toth
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

import spiralcraft.app.Component;
import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.StandardContainer;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;

/**
 * <p>A Service which groups a number of other services together
 * </p>
 * 
 * @author mike
 *
 */
public class ServiceGroup
  extends AbstractComponent
  implements Service
{

  public void setServices(final Service[] services)
  {
    this.childContainer
      =new StandardContainer(this,services)
    {     
      
      @Override
      protected Focus<?> bindChild(Focus<?> context,Component service) 
        throws ContextualException
      { 
        Focus<?> ret=super.bindChild(context,service);
        if (service instanceof Service && ret!=context)
        { 
          context.addFacet(ret);
          selfFocus.addFacet(ret);
        }
        return ret;
      }
    };
  } 
  
}
