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

import java.util.ArrayList;

import spiralcraft.lang.Channel;
import spiralcraft.security.auth.AuthSession;
import spiralcraft.security.auth.Credential;
import spiralcraft.security.auth.PasswordCleartextCredential;
import spiralcraft.security.auth.UsernameCredential;
import spiralcraft.security.auth.ChallengeCredential;

/**
 * <p>Convenience class for supplying login credentials from a user interface
 * </p>
 *
 * <p>This class provides a fascade to the credential system. Credentials
 *   can be supplied through the bean methods, Calling update() will 
 *   effectively replace the credentials in the authentication session with
 *   those specified. 
 * </p>
 * 
 * @author mike
 *
 */
public class LoginEntry
{
  private Channel<AuthSession> sessionChannel;
  private AuthSession session;
  
  private volatile String name;
  private volatile String password;
  private volatile String challenge;
  private volatile byte[] digest;
  private volatile boolean persistent;
  
  
  public LoginEntry(Channel<AuthSession> sessionChannel)
  { this.sessionChannel=sessionChannel;
  }
  
  public LoginEntry(AuthSession session)
  { 
    if (session==null)
    { throw new IllegalArgumentException("AuthSession cannot be null");
    }
    this.session=session;
  }
  
  public void reset()
  { 
    name=null;
    password=null;
    challenge=null;
    digest=null;
    persistent=false;
  }
  
  public void update()
  { 
    AuthSession session=(this.session==null?sessionChannel.get():this.session);
    session.clearCredentials();
    
    ArrayList<Credential<?>> credentials
      =new ArrayList<Credential<?>>();

    
    if (name!=null)
    { credentials.add(new UsernameCredential(name));
    }
    if (password!=null)
    { credentials.add(new PasswordCleartextCredential(password));
    }
    if (digest!=null)
    { credentials.add(new DigestCredential(digest));
    }
    if (challenge!=null)
    { credentials.add(new ChallengeCredential(challenge));
    }
    
    session.addCredentials
      (credentials.toArray(new Credential<?>[credentials.size()]));
  
  }
  
  public void setUsername(String name)
  { this.name=name;
  }
    
  public String getUsername()
  { return name;
  }
    
  public void setPasswordCleartext(String pass)
  { this.password=pass;
  }
    
  public String getPasswordCleartext()
  { return password;
  }
  
  public void setSaltedDigest(byte[] digest)
  {
//    sessionChannel.get().addCredentials
//      (new Credential[] {new DigestCredential(digest)});
    this.digest=digest;
  }
  
  public byte[] getSaltedDigest()
  { return digest;
  }
  
  public void setChallengeCredential(String challenge)
  { 
//    sessionChannel.get().addCredentials
//    (new Credential[] {new ChallengeCredential(challenge)});
    this.challenge=challenge;
    
  }
  
  public String getChallenge()
  { return challenge;
  }
  
  public boolean isPersistent()
  { return persistent;
  }
  
  public void setPersistent(boolean persistent)
  { this.persistent=persistent;
  }
  
  
}