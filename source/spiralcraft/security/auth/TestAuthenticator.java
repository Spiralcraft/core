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

public class TestAuthenticator
  extends Authenticator
{
  public TestAuthenticator()
  { realmName="test@test.test";
  }
  
  public TestSession createSession()
  { return new TestSession();
  }
  
  
}

class TestSession
  extends AuthSession
{

  
  
  public boolean isAuthenticated()
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
    { return true;
    }
    return false;
  }
}
