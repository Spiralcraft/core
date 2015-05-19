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

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.Focus;
import spiralcraft.service.ContextService;
import spiralcraft.util.ArrayUtil;

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
  extends ContextService
{
  
  protected Authority[] authorities=new Authority[0];
  protected ContextResourceMap map
    =new ContextResourceMap();
  
  
  public void setAuthorities(Authority[] authorities)
  { this.authorities=authorities;
  }

  public void addAuthority(Authority authority)
  {
    if (bound)
    { throw new IllegalStateException("Already bound");
    }
    authorities=ArrayUtil.append(authorities,authority);
  }
  
  public Authority getAuthority(String name)
  { 
    for (Authority authority: authorities)
    { 
      if (authority.getAuthorityName().equals(name))
      { return authority;
      }
    }
    return null;
  }
  
  @Override
  public Focus<?> bindExports(Focus<?> focusChain)
    throws ContextualException
  { 
    map.bind(focusChain);
    focusChain=super.bindExports(focusChain);
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
    Lifecycler.start(authorities);
    super.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    super.stop();
    Lifecycler.stop(authorities);
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
  
  @Override
  public void push()
  { 
    if (logLevel.isFine())
    { log.fine("Pushing FileSpace "+toString());
    }
    map.push();
    super.push();

  }
  
  @Override
  public void pop()
  { 
    super.pop();
    map.pop();
    if (logLevel.isFine())
    { log.fine("Popping FileSpace "+toString());
    }
  }

  
  
  
}
