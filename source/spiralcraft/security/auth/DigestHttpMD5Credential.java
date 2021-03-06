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
 * A Digest of a string composed of the username + realm + password,
 *   specified by RFC2617. 
 * 
 * @author mike
 */
public class DigestHttpMD5Credential
  extends Credential<byte[]>
{
  public DigestHttpMD5Credential()
  { super(byte[].class,null);
  }
  
  public DigestHttpMD5Credential(byte[] digest)
  { super(byte[].class,digest);
  }
}
