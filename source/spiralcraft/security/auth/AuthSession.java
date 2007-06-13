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
package spiralcraft.security.auth;

import java.security.Principal;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents the state of the authentication process from a client's
 *   perspective.
 * 
 * @author mike
 */
public abstract class AuthSession
{

  protected final ArrayList<Credential> credentialList
    =new ArrayList<Credential>();
  
  protected Principal principal;
  protected boolean authenticated;
  protected Class[] requiredCredentials;
  
  /**
   * @return The Principal currently authenticated in this session.
   * 
   * <P>In the case of Principal escalation, the most privileged Principal
   *   will be returned.
   */
  public Principal getPrincipal()
  { return principal;
  }
  
  @SuppressWarnings("unchecked") // Required downcast
  protected <T extends Credential> T 
    getCredential(Class<T> clazz)
  {
    for (Credential cred: credentialList)
    { 
      if (cred.getClass()==clazz)
      { return (T) cred;
      }
    }
    return null;
  }
  
  public void addCredentials(Credential[] credentials)
  { 
    for (Credential cred: credentials)
    { 
      Iterator<Credential> it=credentialList.iterator();
      while (it.hasNext())
      { 
        if (it.next().getClass()==cred.getClass())
        { it.remove();
        }
      }
      credentialList.add(cred);
    }
  }
    
  public Class[] getRequiredCredentials()
  { return requiredCredentials;
  }
  
  public abstract boolean isAuthenticated();

  
}
