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
import java.util.List;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.util.StaticInstanceResolver;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.security.auth.Permission;
import spiralcraft.security.auth.Role;
import spiralcraft.util.ListMap;

public class StandardRole
  implements Role
{
  
  private final ClassLog log=ClassLog.getInstance(getClass());
  

  private ListMap<URI,Permission> grantedPermissions
    =new ListMap<URI,Permission>();
  private ListMap<URI,Permission> deniedPermissions
    =new ListMap<URI,Permission>();
  
  private final URI id;
  
  public StandardRole(Tuple t)
    throws DataException
  { 
    id=t.getType().getURI();
    t.getType().fromData(t,new StaticInstanceResolver(this));
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    log.fine("Granted in "+this+": "+grantedPermissions);
    log.fine("Denied in "+this+": "+deniedPermissions);
    return focusChain;
  }

  @Override
  public String getName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI getId()
  { return id;
  }

  @Override
  public Vote vote(
    Permission permission)
  {
    
    List<Permission> deniedPermissionList
      =deniedPermissions.get(permission.getId());
    if (deniedPermissionList!=null 
        && implies(deniedPermissionList,permission)
       )
    { return Vote.DENY;
    }
    
    List<Permission> grantedPermissionList
      =grantedPermissions.get(permission.getId());
    if (grantedPermissionList!=null 
      && implies(grantedPermissionList,permission)
     )
    { return Vote.GRANT;
    }
    
    return Vote.ABSTAIN;
  }
  
  private boolean implies(List<Permission> permissionList, Permission permission)
  { 
    for (Permission assignedPermission : permissionList)
    { 

      log.fine("Checking "+assignedPermission+" -> "+permission);
      if (assignedPermission.implies(permission))
      { 
        return true;
      }
    }
    return false;
  }
  
  public void setGrantedPermissions(Permission[] permissions)
  { 
    for (Permission permission:permissions)
    { grantedPermissions.add(permission.getId(),permission);
    }
  }
  
  public void setDeniedPermissions(Permission[] permissions)
  {
    for (Permission permission:permissions)
    { deniedPermissions.add(permission.getId(),permission);
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+id;
  }

}
