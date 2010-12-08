//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.security.spi;

import java.net.URI;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.security.auth.AuthSession;
import spiralcraft.security.auth.Authorizer;
import spiralcraft.security.auth.Permission;
import spiralcraft.security.auth.Role;
import spiralcraft.util.ArrayUtil;

public abstract class AbstractAuthorizer
  implements Authorizer
{
  protected final ClassLog log=ClassLog.getInstance(AbstractAuthorizer.class);

  protected Role[] authenticatedRoles;
  protected Role[] unauthenticatedRoles;
  protected Channel<AuthSession> sessionChannel;
  protected LinkedHashMap<URI,Role> registeredRoles
    =new LinkedHashMap<URI,Role>();

  public void setRegisteredRoles(Role[] registeredRoles)
  { 
    if (registeredRoles!=null)
    {
      for (Role role: registeredRoles)
      { this.registeredRoles.put(role.getId(),role);
      }
    }
  }
  
  @Override
  public Role[] getRegisteredRoles()
  { 
    return registeredRoles.values().toArray
      (new Role[registeredRoles.values().size()]);
  }
  
  /**
   * 
   * @param roles Roles that all authenticated sessions have 
   *   (eg. permission to sign in)
   */
  public void setAuthenticatedRoles(Role[] roles)
  { this.authenticatedRoles=roles;
  }

  /**
   * Roles that all authenticated sessions have 
   *   (eg. permission to sign in, denied for disabled logins)
   *   
   * @return
   */
  public Role[] getAuthenticatedRoles()
  { return this.authenticatedRoles;
  }
  
  /**
   * 
   * @param roles Roles that all unauthenticated sessions have 
   *   (eg. permission to register)
   */
  public void setUnauthenticatedRoles(Role[] roles)
  { this.authenticatedRoles=roles;
  }
  
  /**
   * Roles that all unauthenticated sessions have 
   *   (eg. permission to register)
   * 
   * @return
   */
  public Role[] getUnauthenticatedRoles()
  { return this.unauthenticatedRoles;
  }

  @Override
  public boolean hasPermission(
    Principal principal,
    Permission permission
    )
  { return vote(getRolesForPrincipal(principal),permission);
  }

  protected boolean vote(Role[] roles,Permission permission)
  {     
    if (roles!=null)
    {
      Role.Vote vote=null;
      for (Role role:roles)
      {
        switch (role.vote(permission))
        {
          case DENY:
            return false;
          case GRANT:
            vote=Role.Vote.GRANT;
        }
        
        if (vote==Role.Vote.GRANT)
        { log.fine("Permission granted in "+role.getId()+": "+permission);
        }
        else
        { log.fine("Permission not granted in "+role.getId()+": "+permission);
        }
      }

      return vote==Role.Vote.GRANT;
    }
    else
    { return false;
    }

  }
  
  
  /**
   * <p>Indicate whether the specified permission has been effectively granted
   *   for this AuthSession, through either the Roles assigned to the
   *   authenticated Principal, or through any Roles associated with 
   *   the AuthSession itself.
   * </p>
   * 
   * @param permission
   * @return
   */
  @Override
  public boolean hasPermission(AuthSession session,Permission permission)
  { 
    if (permission==null)
    { throw new IllegalArgumentException("Permission is null");
    }
    Role[] roles;
    Principal principal
      =session.isAuthenticated()?session.getPrincipal():null;
      
    if (principal==null)
    { roles=unauthenticatedRoles;
    }
    else
    { 
      roles=ArrayUtil.concat
        (authenticatedRoles
        ,getRolesForPrincipal(principal)
        );
    }
    return vote(roles,permission);
    
  }
  
  protected Role[] getRolesByIds(URI[] roleIds)
  {
    if (roleIds==null)
    { return null;
    }
    LinkedHashSet<Role> roles=new LinkedHashSet<Role>();
    for (URI uri:roleIds)
    { 
      Role role=registeredRoles.get(uri);
      if (role==null)
      { log.warning("Role id "+uri+" is not registered");
      }
      else
      { roles.add(role);
      }
    }
    return roles.toArray(new Role[roles.size()]);
    
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    sessionChannel=LangUtil.findChannel(AuthSession.class,focusChain);

    for (Role role: registeredRoles.values())
    { role.bind(focusChain);
    }
    
    return focusChain;
  }
  
}
