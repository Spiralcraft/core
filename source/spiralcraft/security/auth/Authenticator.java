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


/**
 * <P>Authenticates a client using one or more sets of credentials. 
 *   
 * <P>An Authenticator provides an AuthSession to handle the exchange
 *   of credentials and to communicate the status of the authentication process
 *   to the client.
 */
public abstract class Authenticator
{
  protected String realmName;
  
  /**
   * @return The name of the realm this Authenticator will be serving.
   * 
   * <P>The realm name should be as unique and as stable as possible. Specific
   * paths in a web tree should be avoided, as the realm name is a factor in
   * authentication cryptography, and may require that all passwords are reset
   * if it is changed.
   * 
   * <P>ie. MyRealmName@MyDomain.com
   * 
   */
  public String getRealmName()
  { return realmName;
  }
  
  /**
   * <P>Create a new AuthSession, which represents a login session in the
   *   realm of the Authenticator.
   */
  public abstract AuthSession createSession();
  
}





