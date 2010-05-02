//
//Copyright (c) 1998,2010 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.vfs.context;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * <p>Manages and provides access to a set of VFS Resources by publishing
 *   Authorities into the application context Focus chain and resource mapping.
 * </p>
 * 
 * <p>Authorities are published into the "context:" scheme by default
 * </p>
 *  
 * @author mike
 *
 */
public class FileSpace
  implements FocusChainObject,Lifecycle
{
  
  protected Authority[] authorities;
  protected ContextResourceMap map
    =new ContextResourceMap();
  
  
  public void setAuthorities(Authority[] authorities)
  { this.authorities=authorities;
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  { 
    focusChain=focusChain.chain(new SimpleChannel<FileSpace>(this,true));
    for (Authority authority:authorities)
    { 
      authority.bind(focusChain);
      map.put(authority);
    }  
    
    return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  {
    for (Authority authority:authorities)
    { authority.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    for (Authority authority:authorities)
    { authority.stop();
    }
    
  }

  /**
   * <p>Return a Graft managed within this FileSpace, usually for the purpose
   *   of interacting with a management interface.
   * </p>
   * 
   * @param authorityName
   * @param path
   */
  public Graft getGraft(String authorityName,String path)
  { return map.getGraft(authorityName,path);
  }
  
  public void push()
  { map.push();
  }
  
  public void pop()
  { map.pop();
  }
  
}
