package spiralcraft.security;

import java.security.Permission;

import spiralcraft.main.Spiralcraft;

public class SystemSecurityManager
  extends SecurityManager
{
  public SystemSecurityManager()
  {
    if (Spiralcraft.DEBUG)
    { System.err.println("SystemSecurityManager<init>()");
    }
  }
  
  public void checkPermission(Permission perm)
  { 
    if (Spiralcraft.DEBUG)
    { System.err.println("SystemSecurityManager.checkPermission("+perm+")");
    }
    
    super.checkPermission(perm);
  }
}
