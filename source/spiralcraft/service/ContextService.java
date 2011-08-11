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
import spiralcraft.lang.Context;
import spiralcraft.lang.Focus;
import spiralcraft.util.ArrayUtil;

/**
 * A Service which publishes a bundle of other Services as well as any
 *   thread contextual references
 * 
 * @author mike
 *
 */
public class ContextService
  extends AbstractComponent
  implements Service,Context
{
  private Context[] threadContextuals
    =new Context[0];  
  
  

  public void setServices(final Service[] services)
  {
    this.childContainer
      =new StandardContainer()
    {
      { 
        children=new Component[services.length];
        int i=0;
        for (Component service:services)
        { children[i++]=service;
        }
      }
      
      
      @Override
      protected Focus<?> bindChild(Focus<?> context,Component service) 
        throws ContextualException
      { 
        Focus<?> ret=super.bindChild(context,service);
        if (service instanceof Service && ret!=context)
        { context.addFacet(ret);
        }
        if (service instanceof Context)
        { 
          threadContextuals
            =ArrayUtil.append(threadContextuals,(Context) service);
        }
        return ret;
      }
    };
  } 
  
  @Override
  public void push()
  {
    for (Context tc : threadContextuals)
    { tc.push();
    }
  }

  @Override
  public void pop()
  {
    for (Context tc : threadContextuals)
    { tc.pop();
    }
  }
}
