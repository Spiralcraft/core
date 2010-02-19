//
//Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.security.auth;

/**
 * <p>A token determined by the server to be returned by the client.
 * </p>
 * 
 * <p>Often used as part of a salt in a message digest to establish proof of
 *   possession of a token without transmitting an easily forged message.
 * </p>
 * 
 * <p>May or may not be actually transmitted to the client by a server, such
 *   as when a common reference is employed.
 * </p>

 * <p>May be associated with other Credential values including the (incomplete) 
 *   ChallengeResponseCredential.
 * </p>
 * 
 * @author mike
 */
public class ChallengeCredential
  extends Credential<String>
{
  
  public ChallengeCredential()
  { super(String.class,null);
  }
  
  public ChallengeCredential(String username)
  { super(String.class,username);
  }
}
