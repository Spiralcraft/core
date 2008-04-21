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

import spiralcraft.lang.Channel;
import spiralcraft.security.auth.AuthSession;
import spiralcraft.security.auth.Credential;
import spiralcraft.security.auth.PasswordCleartextCredential;
import spiralcraft.security.auth.UsernameCredential;

/**
 * Convenience class for supplying login credentials from a user interface
 * 
 * @author mike
 *
 */
public class LoginEntry
{
  private Channel<AuthSession> sessionChannel;
  
  private String name;
   
  public LoginEntry(Channel<AuthSession> sessionChannel)
  { this.sessionChannel=sessionChannel;
  }
  
  public void setUsername(String name)
  { 
    sessionChannel.get().addCredentials
      (new Credential[] {new UsernameCredential(name)});
    this.name=name;
  }
    
  public String getUsername()
  { return name;
  }
    
  public void setPasswordCleartext(String pass)
  { 
    sessionChannel.get().addCredentials
      (new Credential[] {new PasswordCleartextCredential(pass)});
  }
    
  public String getPasswordCleartext()
  { return null;
  }
}