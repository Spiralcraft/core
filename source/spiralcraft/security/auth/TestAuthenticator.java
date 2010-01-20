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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

public class TestAuthenticator
  extends Authenticator
{
  public TestAuthenticator()
  { realmName="test@test.test";
  }
  
  @Override
  public TestSession createSession()
  { return new TestSession(this);
  }
  
  
}

class TestSession
  extends AuthSession
{

  protected volatile Principal principal;
  protected volatile boolean authenticated;
  
  public TestSession(TestAuthenticator authenticator)
  { super(authenticator);
  }
  
  @Override
  public boolean authenticate()
  {    
    UsernameCredential username
      =getCredential(UsernameCredential.class);
    PasswordCleartextCredential password
      =getCredential(PasswordCleartextCredential.class);
    
    if (username!=null
        && "test".equals(username.getValue())
        && password!=null
        && "test".equals(password.getValue())
        )
    { 
      authenticated=true;
    }
    else
    { authenticated=false;
    }
    return authenticated;
  }

  @Override
  public byte[] opaqueDigest(
    String input)
  {
    try
    { 
      return MessageDigest.getInstance("SHA-256").digest
        ((authenticator.getRealmName()+input).getBytes());
    }
    catch (NoSuchAlgorithmException x)
    { throw new RuntimeException("SHA256 not supported",x);
    }
    
    

  }
  
  /**
   * @return The Principal currently authenticated in this session.
   * 
   * <P>In the case of Principal escalation, the most privileged Principal
   *   will be returned.
   */
  @Override
  public synchronized Principal getPrincipal()
  { return principal;
  }
  
  @Override
  public synchronized  boolean isAuthenticated()
  { 
    return authenticated;
  }    
}
