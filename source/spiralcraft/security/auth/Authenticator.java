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

import java.util.HashMap;
import java.util.Map;

import spiralcraft.lang.BeanFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;


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
  protected ThreadLocalChannel<AuthSession> sessionChannel;
  protected Focus<AuthSession> sessionFocus;
  protected final HashMap<String,Credential<?>> protoMap
    =new HashMap<String,Credential<?>>();
  protected Class<? extends Credential<?>>[] acceptedCredentials;
  
  protected CompoundFocus<Map<String,Credential<?>>> credentialFocus;
  
  protected void setAcceptedCredentials
    (Class<? extends Credential<?>>[] acceptedCredentials)
  { 
    this.acceptedCredentials=acceptedCredentials;
    for (Class<? extends Credential<?>> credClass: acceptedCredentials)
    { 
      if (protoMap.get(credClass.getSimpleName())==null)
      { 
        try
        { protoMap.put(credClass.getSimpleName(),credClass.newInstance());
        }
        catch (InstantiationException x)
        { 
          throw new IllegalArgumentException
            ("Credential class "
            +credClass.getName()
            +" could not be instantiated: "
            +x
            ,x
            );
        }
        catch (IllegalAccessException x)
        {
          throw new IllegalArgumentException
          ("Credential class "
          +credClass.getName()
          +" could not be instantiated: "
          +x
          ,x
          );
        }
      }
    }
  }  
  
      
  public Class<? extends Credential<?>>[] getRequiredCredentials()
  { return acceptedCredentials;
  }
  
  /**
   * @return The name of the realm this Authenticator will be serving.
   * 
   * <P>The realm name should be as unique and as stable as possible. 
   * Application names that may change (eg. paths in a web tree) should be 
   * avoided, as the realm name is a factor in
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
  
  /**
   * <p>Provide the Authenticator with a Focus for it to resolve data sources
   *   and other resources from the context. The context can be null, as long
   *   as no contextual resources are required by the specific Authenticator.
   * </p>
   *   
   * @param context
   * @throws BindException
   */
  public void bind(Focus<?> context)
    throws BindException
  { 
    this.sessionChannel
      =new ThreadLocalChannel<AuthSession>
        (BeanReflector.<AuthSession>getInstance(AuthSession.class));
    
    if (context!=null)
    { this.sessionFocus=new SimpleFocus<AuthSession>(context,sessionChannel);
    }
    else
    { this.sessionFocus=new SimpleFocus<AuthSession>(sessionChannel);
    }
        
    credentialFocus
      =new CompoundFocus<Map<String,Credential<?>>>
      (sessionFocus,new CredentialSetChannel(protoMap,sessionChannel)
      );
    credentialFocus.bindFocus
      ("",new BeanFocus<Authenticator>(this));
  }
  
}





