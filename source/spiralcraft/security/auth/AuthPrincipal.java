package spiralcraft.security.auth;

import java.security.Principal;

public interface AuthPrincipal
  extends Principal
{

  /**
   * The internal unique identifier for this account used to link
   *   application specific identities to this principal.
   * 
   * @return
   */
  String getId();
}
