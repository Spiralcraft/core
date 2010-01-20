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
package spiralcraft.security.auth;

import java.security.Principal;

import spiralcraft.lang.FocusChainObject;

/**
 * Authenticates a set of credentials
 * 
 * @author mike
 *
 */
public interface AuthModule
  extends FocusChainObject
{
  /**
   * The name by which clients will reference the state
   *   of a specific AuthModule.Session
   * 
   * @return
   */
  public String getName();
  
  /**
   * Authenticate the credentials provided to the AuthSession
   * 
   * @return A Session, if the module has anything to contribute to the
   *   authentication process. 
   */
  Session createSession();
  
  public interface Session
  {
    public boolean authenticate();
    
    public void logout();
    
    public boolean isAuthenticated();
    
    public void refresh();
    
    public void credentialsChanged();
    
    public Principal getPrincipal();
  }
}
