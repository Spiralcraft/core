//
// Copyright (c) 1998,2007 Michael Toth
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

import java.net.URI;
import java.security.Principal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * <P>Represents the state of the authentication process from a client's
 *   perspective.
 *   
 * <P>Provides an interface to the credentials received from the client
 *   for authentication functionality.
 * 
 * @author mike
 */
public abstract class AuthSession
{
  public static final URI FOCUS_URI
    =URI.create("class:/spiralcraft/security/auth/AuthSession");

  protected final ArrayList<Credential<?>> credentialList
    =new ArrayList<Credential<?>>();
  
  protected final HashMap<String,Credential<?>> credentialMap
    =new HashMap<String,Credential<?>>();
  
  protected Principal principal;
  protected boolean authenticated;
  protected boolean sticky;
  
  

  
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
  
  /**
   * Called internally to retreived the Credential map
   * 
   * @return
   */
  Map<String,Credential<?>> getCredentialMap()
  { return credentialMap;
  }
    
  public void addCredentials(Credential<?>[] credentials)
  { 
    principal=null;
    authenticated=false;
    for (Credential<?> cred: credentials)
    { 
      Iterator<Credential<?>> it=credentialList.iterator();
      while (it.hasNext())
      { 
        if (it.next().getClass()==cred.getClass())
        { it.remove();
        }
      }
      credentialList.add(cred);
      credentialMap.put(cred.getClass().getSimpleName(),cred);
    }
  }

  
  public abstract boolean isAuthenticated();
  
  /**
   * Logs out the user by clearing all credentials and principal data and
   *   setting authenticated to false.
   */
  public void logout()
  {
    credentialList.clear();
    credentialMap.clear();
    principal=null;
    authenticated=false;
    
  }
  
}
