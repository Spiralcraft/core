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
package spiralcraft.security;

import java.security.Permission;

import spiralcraft.log.ClassLog;


/**
 * Stub for Classloader level permissions implementation
 *
 * Defunct now due to the removal of SecurityManager from Java core
 *  
 * @author mike
 */
public class SystemSecurityManager
{
  private static final ClassLog log
    =ClassLog.getInstance(SystemSecurityManager.class);
  
  public static boolean DEBUG;
  
  public SystemSecurityManager()
  {
    if (DEBUG)
    { log.fine("SystemSecurityManager<init>()");
    }
  }
  
  public void checkPermission(Permission perm)
  { 
    if (DEBUG)
    { log.fine("SystemSecurityManager.checkPermission("+perm+")");
    }
    
  }
}
