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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.BeanFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
//import spiralcraft.log.ClassLog;


/**
 * <p>Authenticates a client using one or more sets of credentials. 
 * </p>
 *   
 * <p>An Authenticator provides an AuthSession to handle the exchange
 *   of credentials and to communicate the status of the authentication process
 *   to the client.
 * </p>
 */
public class Authenticator
  implements FocusChainObject
{
  public static final URI FOCUS_URI
    =BeanReflector.getInstance(Authenticator.class).getTypeURI();

//  private static final ClassLog log
//    =ClassLog.getInstance(Authenticator.class);
  
  protected String realmName;
  protected ThreadLocalChannel<AuthSession> sessionChannel;
  protected Focus<AuthSession> sessionFocus;
  protected final HashMap<String,Credential<?>> protoMap
    =new HashMap<String,Credential<?>>();
  protected Class<? extends Credential<?>>[] acceptedCredentials;
  
  protected Focus<Map<String,Credential<?>>> credentialFocus;
  protected AuthModule[] authModules;
  protected boolean debug;

  
      
//  public Class<? extends Credential<?>>[] getRequiredCredentials()
//  { return acceptedCredentials;
//  }
//  
  /**
   * @return The name of the realm this Authenticator will be serving.
   * 
   * <P>The realm name should be as unique and as stable as possible. 
   * Application names that may change (eg. paths in a web tree) should be 
   * avoided, as the realm name is a factor in
   * authentication cryptography, and may require that all passwords be reset
   * or cause other data loss if it is changed.
   * 
   * <P>ie. MyRealmName@MyDomain.com
   * 
   */
  public String getRealmName()
  { return realmName;
  }
  
  public void setRealmName(String realmName)
  { 
    if (sessionChannel!=null)
    { throw new IllegalStateException("Cannot change realm name after binding");
    }
    this.realmName=realmName;
  }
  
  /**
   * <P>Create a new AuthSession, which represents a login session in the
   *   realm of the Authenticator.
   */
  public AuthSession createSession()
  { 
    
    AuthSession session=new AuthSession(this);
    session.setDebug(debug);
    return session;
  }
  
  AuthModule[] getAuthModules()
  { return authModules;
  }
  
  public void setAuthModules(AuthModule[] authModules)
  { this.authModules=authModules;
  }
  
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>Provide the Authenticator with a Focus for it to resolve data sources
   *   and other resources from the context. The context can be null, as long
   *   as no contextual resources are required by the specific Authenticator.
   * </p>
   *   
   * @param context
   * @throws BindException
   */
  public Focus<?> bind(Focus<?> context)
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
    sessionFocus.addFacet(new BeanFocus<Authenticator>(this));
    
    credentialFocus
      =sessionFocus.chain(new CredentialSetChannel(protoMap,sessionChannel));

    if (authModules!=null)
    {
      for (AuthModule authModule:authModules)
      { authModule.bind(credentialFocus);
      }
    }
    return sessionFocus;
  }
  
  public void pushSession(AuthSession session)
  { sessionChannel.push(session);
  }
  
  public void popSession()
  { sessionChannel.pop();
  }
  
  
  protected void setAcceptedCredentials
    (Class<? extends Credential<?>>[] acceptedCredentials)
  { 
    this.acceptedCredentials=acceptedCredentials;
    registerCredentials(acceptedCredentials);
  }
  
  public void registerCredentials(Class<? extends Credential<?>>[] classes)
  {
    if (classes==null)
    { return;
    }
    for (Class<? extends Credential<?>> credClass: classes)
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
  
}





